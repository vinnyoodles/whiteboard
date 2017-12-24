## whiteboard-android [CS 3714 - Final Project]

#### Motivation
The idea behind this application came from personal experiences where I had difficulty explaining my approach to an technical interview type problem to my friends. 
This situation would come up when my friends and I would conduct mock interviews on one another. 
This is trivial when conducting a mock interview in person, but more complex when conducting it remotely. 

#### Purpose
The purpose of this mobile application is to allow students and future software engineers to study and prepare for technical interview remotely with their peers. 
This clears the obstacle of having to be in the same physical location to practice for technical interviews. 
The purpose of this mobile application can be expanded to numerous use cases from remote teaching to working in a collaborative setting.

#### Google Play Store
[Collaboard](https://play.google.com/store/apps/details?id=com.vinnyoodles.vincent.whiteboardclient&hl=en)

#### Project Requirements

- [x] Communication via network. This could be accomplished over Bluetooth/Wifi/4G. The core idea here is that you send and receive data and, thus, implement long running operations.
- [x] Persistence. Your app needs to use some form of persistent storage (Some examples: SQLite, File, SharedPreferences, saveInstanceState callbacks).
- [x] Background operations. You must use background threads (AsyncTask, Thread or any other variants) to execute long running tasks.
- [x] Service. Your app should have a Service that will continue running in the background after you exit the activity.
- [x] Retained fragment. As your project app is going to contain long running operations, you must use retained fragments to retain references to those long running operations.
- [x] BroadcastReceivers. You will need to use at least one BroadcastReceiver to pass/receive information (Example: ON_BOOT to launch your background service).
- [x] Fragments. Use at least two fragments to display UI and manage them from within the containing activity.
- [x] Location. You can use either LocationManager or Google Play Services to retrieve location.
- [x] Camera/multimedia/audio. You can capture/play/edit photo/video/audio.

#### Screenshots
<img src='images/image1.png' title='img1' alt='Image' />

<img src='images/image2.png' title='img1' alt='Image' />

<img src='images/image3.png' title='img1' alt='Image' />
