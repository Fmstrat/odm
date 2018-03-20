# Open Device Manager for Android

Open Device Manager, or ODM, is a fully free, open source, end-to-end encrypted alternative to Google.s Android Device Manager that can be installed in right around 10 seconds. Building on the look and feel of the original ADM, ODM focuses on supporting additional features while giving users the security and privacy of a self-hosted, open source platform.

ODM was created because of the lack of fully open source and secure device managers on the market. While there are solutions that offer open source web components, the Android Applications are not, and are known to use Google Analytics or ad networks that pull information such as IP address, all of which eliminate 100% privacy. ODM overcomes this by offering everything up for grabs on github. We will also be submitting the github repository to F-Droid, an alternative App Store that compiles open source applications directly from github, so you know you.re getting a safe APK.

Features include:

- Open source web interface
- Open source Android Application
- All notifications/commands sent through Google are encrypted first
- Full AJAX interface
- 10 second server install
- Multi-user support
- Multi-device per user support
- Last and previous locations
- Google maps integration
- Lock device
- Take photos with rear and front cameras
- Alert ringer to locate the phone
- Receive an SMS to identify insertion of new SIM
- Send a custom notification
- Wipe device
- Log of all previous activity


## Installation

Installation is simple and straightforward. Use the below or watch the video embedded above.

- Extract the web archive to any folder on a system running PHP.
- Edit include/config.php to turn on registrations (and change any other settings you would like): $ALLOW_REGISTRATIONS = true
- Open mysql: mysql
- Create the database: create database odm;
- Exit mysql: exit;
- Import database structure: mysql odm < sql/odm.sql

Also, be sure php5-mcrypt and php5-curl are installed on your system.

Please pose any questions or discussion to the thread at: http://forum.xda-developers.com/showthread.php?t=2601720


## Configuration

The following variables can be edited in config.php.

Sets the database connection information:
```
$DB_HOST = "localhost";
$DB_USER = "root";
$DB_PASSWORD = "";
$DB_DATABASE = "odm";
```

Whether or not to allow user registration. This must be true to create the first user, but can be disabled after that for security.
```
$ALLOW_REGISTRATIONS = false;
```

## To-Do

- User testing
- Fix bug in Android 4.0+ that crashes some devices when using the camera in a service
- Improve new user registration process with email validation
- Expert security audit of encryption scheme (I am not a security expert, and used samples provided online for the PHP to android methodology)


## Change Log

**v0.01 beta**

- Beta release (see To-Do)
