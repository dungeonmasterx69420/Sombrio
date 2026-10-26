#!/usr/bin/env bash
# Starts a Jellyfin server for testing on port 8096, with a user "tester" (password "test") and a music library of
# three generated tracks, "Track 1" to "Track 3" by TestArtist on TestAlbum, and waits until it has scanned them.
#
#     .github/scripts/jellyfin-test-server.sh [directory]
#
# The server keeps its configuration and media in the directory (by default jellyfin-test-server), and runs in Docker
# as the container jellyfin-test-server.
set -euo pipefail

dir="$(realpath -m "${1:-jellyfin-test-server}")"
server=http://127.0.0.1:8096
client='MediaBrowser Client="test", Device="test", DeviceId="jellyfin-test-server", Version="1.0"'
mkdir -p "$dir/config" "$dir/cache" "$dir/media/music"

docker run --detach --name jellyfin-test-server --network host \
  --volume "$dir/config:/config" --volume "$dir/cache:/cache" --volume "$dir/media:/media" \
  jellyfin/jellyfin:10.10.7 > /dev/null

# Made with the server's own ffmpeg, so nothing else needs installing
album=/media/music/TestArtist/TestAlbum
docker exec jellyfin-test-server mkdir -p "$album"
for n in 1 2 3; do
  docker exec jellyfin-test-server /usr/lib/jellyfin-ffmpeg/ffmpeg -loglevel error -y \
    -f lavfi -i "sine=frequency=$((300 + n * 100)):duration=40" \
    -metadata title="Track $n" -metadata artist=TestArtist -metadata album_artist=TestArtist \
    -metadata album=TestAlbum -metadata track="$n" -codec:a libmp3lame -b:a 128k "$album/0$n Track $n.mp3"
done

for _ in $(seq 1 90); do
  curl -sf "$server/System/Info/Public" | grep -q '"StartupWizardCompleted"' && break
  sleep 2
done

# The first-run wizard: language, the user, remote access
post() { curl -sf -X POST "$server$1" -H 'Content-Type: application/json' -H "Authorization: $client${token:+, Token=\"$token\"}" -d "${2:-}"; }
post /Startup/Configuration '{"UICulture":"en-US","MetadataCountryCode":"US","PreferredMetadataLanguage":"en"}'
curl -sf "$server/Startup/User" > /dev/null
post /Startup/User '{"Name":"tester","Password":"test"}'
post /Startup/RemoteAccess '{"EnableRemoteAccess":true,"EnableAutomaticPortMapping":false}'
post /Startup/Complete

token="$(post /Users/AuthenticateByName '{"Username":"tester","Pw":"test"}' |
  python3 -c 'import json, sys; print(json.load(sys.stdin)["AccessToken"])')"
post '/Library/VirtualFolders?name=Music&collectionType=music&refreshLibrary=true' \
  '{"LibraryOptions":{"PathInfos":[{"Path":"/media/music"}]}}'

for _ in $(seq 1 60); do
  count="$(curl -sf "$server/Items?Recursive=true&IncludeItemTypes=Audio" -H "Authorization: $client, Token=\"$token\"" |
    python3 -c 'import json, sys; print(json.load(sys.stdin)["TotalRecordCount"])')"
  [ "$count" = 3 ] && break
  sleep 2
done
echo "Jellyfin is up on $server with $count tracks."
[ "$count" = 3 ]
