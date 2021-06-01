
# Email Tracker
Get notified when your email gets read by using a tracking pixel and a web server.

## How it works
We send an email with a 1x1 transparent tracking pixel in it. When the receiver opens the email. their browser will send a GET request to our server, which will process this and send us a text message alert that our email was opened.

## How to DIY

### 1. Setup
1. Setup your webserver with Amazon AWS and nginx: https://medium.com/@nathan_149/install-nginx-on-amazon-ec2-in-5-minutes-get-a-web-server-up-and-running-3516fd06b76
2. Download this git repo

### 2. Twilio
On the webserver:
1. Make a Twilio account
2. In shell, edit env varliables with:

    export TWILIO_ACCOUNT_SID="YOUR_SID"

    export TWILIO_ACCOUNT_AUTH="YOUR_AUTH"

based on your respective SID and AUTH from Twilio

### 3. Server Listener
On the web server:

1. Edit `TO_NUM` and `FROM_NUM` variables to be your to and from numbers.
2. Compile listener with:

    javac -cp .:twilio-8.9.0-jar-with-dependencies.jar EmailTracker.java

2. Run listener on port 10008 (up to you) with:

    java -cp .:twilio-8.9.0-jar-with-dependencies.jar EmailTracker 10008


### 4. Send email

On any device:
1. Fill in credentials in cred.py
2. Change the `LINK` of the tracking pixel to be your server's IP address
2. Edit email subject and text
3. Send the email by running `python3 send_email.py`

### 5. Done!
Now when the receiver opens the email, you'll get a text letting you know that it was opened!


## Chrome Extension
If you want to track your emails, you can also use this free Chrome extension: https://chrome.google.com/webstore/detail/email-tracker/bnompdfnhdbgdaoanapncknhmckenfog

