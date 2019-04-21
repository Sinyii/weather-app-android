# weather-app-android

## API key
Please modify the WEATHERMAP_API_KEY variable in MainActivity with your weather map api key.  
PATH:　weatherAppTab/app/src/main/java/com/sinyi/weatherapptab/MainActivity.java  

## Compile
Please put this directory under your default Android Studio project directory.
Please run it on API 28.

## Functions have built
- Show current temperature.
- Show hourly forecast(24hr).
- Show daily forecast(5 days).
- Can locate user location.
- Can set notifation(half finished)

## Functions haven't built
- Use user location, I am using hardcoded location:Boston right now. The problem is about FragmentPagerAdapter.
- Push notification.


### Screenshots
<img src="img/1.png" width="250">
<img src="img/2.png" width="250">
<img src="img/3.png" width="250">
<img src="img/4.png" width="250">
<img src="img/5.png" width="250">
<img src="img/6.png" width="250">

## References
https://youtu.be/EZ-sNN7UWFU  
https://youtu.be/oh4YOj9VkVE  
https://youtu.be/bNpWGI_hGGg  
https://youtu.be/h4HwU_ENXYM  
https://youtu.be/w1g9AaDltUM  
https://youtu.be/rKnzzrdhb9g  

Modify tab style[https://materialdoc.com/components/tabs/] 
Notifications Tutorial Part 1 - NOTIFICATION CHANNELS - Android Studio Tutorial[https://www.youtube.com/watch?v=tTbd1Mfi-Sk] 
https://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay 
https://codinginflow.com/tutorials/android/notifications-notification-channels/part-1-notification-channels

### Other
- This project started on 4/17/2019, first version uploaded on 4/19/2019.
I will keep modify it.
- The forecast service of openweathermap will only support until5/1/2019.

### 4/21 Update note
- modify display
- built setting activity
- remaining problems: push notification & use user location