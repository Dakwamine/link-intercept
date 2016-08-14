# link-intercept
An Android app which can be opened like any browser to show the link's URL and lets you choose if you want to follow it. Also adds itself in the home-button-swipe-up-action list to disable the action (useful for Android &lt; 6).

## How it works
This application adds itself in the browsers list when touching a link to a http:// or https:// resource. This is set in the AndroidManifest.xml file, in the intent-filter section of MainActivity.

This application adds itself in the home-button-swipe-up-action list, aka assist functionality. This is set in the AndroidManifest.xml file, in the EmptyActivity section.

## Additional information
When installed, this app does not add itself in the applications list (LAUNCHER category is not included in the manifest) simply because it is not useful.