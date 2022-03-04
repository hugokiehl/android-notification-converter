# Android Notification Converter

This is a working scratch of an Android app which listens for notifications and creates new custom notifications out of the initial one. 

The only reason I created this app is that I could not see the images of the notifications from my Nest cameras in my Samsung Galaxy Watch Active 2 (GWA2), just a text like "Your Front Door camera noticed some activity" in my watch was totally useless, and that was bothering me for a long while. So, I decided to make this little app. It's a crappy code, but it solves my problem.

The main images in a Nest camera notification are a sequence of images (so you can see some movement in between the frames in your phone), and those images are available in a location which the GWA2 doesn't use. There is also a single camera frame/image under in an EXTRA_PICTURE which becomes available if you create a WearableExtender out of the source notification, but that EXTRA_PICTURE is also not consumed by GWA2, however, it's good to be used as a "bigPicture" in a regular Notification, and that, yes, gets consumed by the GWA2, **hurray!**

Hence, this app just extracts data from the source notification and creates a new simple/standard notification which works great with my GWA2.

With the current version of this app, I'm able to get all Nest camera notifications, and see their texts and images, so, I had no need to improve it so far, but maybe someone out there is also looking for a way to solve the same problem I had, or maybe have some ideas and wants to extend this app. (*You read this far, so, yeah, go for it and have fun!*)


<img src="https://user-images.githubusercontent.com/3311313/156721887-98af5597-b0fd-400c-8eed-49641446ab62.jpg" width="400" height="598"/> <img src="https://user-images.githubusercontent.com/3311313/156721902-69cb44b7-19fe-442d-941b-69cae9da2d1f.jpg" width="400" height="598"/>

Note that once you install it into your phone, you will need to find the app in the launcher and open it once and grant permission so it can retrieve notification images.

Pros: 
- Nest camera notifications show up with images in my GWA2.
- Once installed, it simply works. 

Cons:
- The new notifications don't show movement / multiple frames as the original Nest notification does - GWA2 does not support it (maybe updating the image of an existing notification in a loop would be interesting - but I don't believe it works for GWA2) 
- Nest notifications get duplicated, but it's possible to modify this app to clean up the source, but then you'll loose the cool Nest notifications with "movement" in your phone. It's possible to configure the GWA2 to not send the original Nest notifications to the watch, so no duplicated notifications in there. 
