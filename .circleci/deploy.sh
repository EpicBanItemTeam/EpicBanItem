#!/bin/bash

print_changelog_text() {
    full_changelog_url="https://euonmyoji.github.io/EpicBanItem---Sponge/#/en/changelog"
    echo "The full changelog is available on <$full_changelog_url>."; echo
    cat CHANGELOG.md | while read -r line; do
        if [[ $line == \#\#* ]]; then context=$line; fi
        if [[ $context == \#\#*$CIRCLE_TAG ]]; then echo "$line"; fi
    done
}

print_changelog_text >CHANGELOG-$CIRCLE_TAG.md

file=$(find build/libs/ -regex "build/libs/EpicBanItem-${CIRCLE_TAG:1}-\w+.jar")
sig=$(find build/libs/ -regex "build/libs/EpicBanItem-${CIRCLE_TAG:1}-\w+.jar.asc")

curl https://ore.spongepowered.org/api/v1/projects/EpicBanItem/versions/${CIRCLE_TAG:1} \
    -F "pluginFile=@$file" -F "pluginSig=@$sig" -F "changelog=@CHANGELOG-$CIRCLE_TAG.md" \
    -F "apiKey=$BUILD_DEPLOYMENT_KEY" -F "channel=PreRelease" -F "recommended=true" -F "forumPost=true"

