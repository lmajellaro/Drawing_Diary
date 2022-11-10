# The Challenge of the Month: Drawing Diary    

<p align="center">
<a href="http://www.youtube.com/watch?feature=player_embedded&v=3zuPS-g1reQ
" target="_blank"><img src="http://img.youtube.com/vi/3zuPS-g1reQ/0.jpg" 
alt="The Challenge of the Month: Drawing Diary " width="480" height="360" border="10" /></a>
</p>  

<p align="center">
<a href="https://play.google.com/store/apps/details?id=drawingDiary.brainlatch.com.drawingDiary&hl=en_IE&gl=US&pli=1">View the Google Play Page </a> 
</p> 

# Table of Contents
1. [Summary](#Summary)
2. [Info](#Info)
3. [Features](#Features)
4. [Specs](#Specs)
5. [How to import and build in Android Studio](#How-to-import-and-build-in-Android-Studio)
6. [Additional Notes](#Additional-Notes)

## Summary   
This app was created back in 2017 with the purpose of providing a tool for artists to practice their skill on the go.
The application provides several training prompts plus the possibility to create custom prompts, take a picture and archive sketches and share them on instagram.    
     
At the time of the first release there were just a few apps of this type while at the time of this writing (2022) the market is flooded with many alternative options offering a wide broad of features.     

## Info    
Type of application: Android application.    
First release date: 28 Sept 2017.    
Last released update: 18 Nov 2020.    
Tested on: Android 11 (API level 30).   

## Features
- Pick one of the preloaded prompts and train your drawing skills every day following the suggested topic for the day.  
- Create new custom prompts.   
- Add notes, comments and ideas to the sketch of the day.   
- Take a picture of your sketch to archive it or share it on instagram.  
- Keep track of your monthly progresses.    

## Specs
Main UI elements: Drop down menus, spinner menus, checkboxes, images, gallery.    
Databases: SQLite.    
Integrations/Permissions:     
- Device camera.    
- Device storage.    
- Instagram via API (if installed on the device).
- Google Admob (banners, interstitials)
- In-app purchase (Ads free)

## How to import and build in Android Studio
(Updated 10.11.2022)
- Donwload the zip package from Github to your local disk
- Uncompress the file 
- On Android Studio:
    - Click on File -> Open
    - Locate the uncompressed folder and select the *app* folder
    - Click ok
    - From the Run menu, select Run 'app'
    - The application will be built and run within the emulator (if configured in Android Studio)
    - To add a new emulated device:
         - Open *SDK Manager*
         - Select an Android SDK (e.g. Android 11.0 (R))
         - Click apply and install the selected SDK
         - Open *AVD Manager*
         - Select *Create Virtual Device*
         - Pick up a device (e.g. Phone -> Pixel XL)
         
In case of errors during the build, you would probably need to update some of the packages used in the build.gradle file or some properties in the gradle.properties file

## Additional Notes   
You might need to tweak the build.gradle file and gradle.properties to build the application properly due to possible deprecated packages.     
