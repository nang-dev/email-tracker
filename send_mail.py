#############
## IMPORTS ##
#############
import time
import smtplib
import datetime
from shutil import copyfile
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import cred

# Based on @mananapr

YOU = cred.my_email
PASS = cred.my_password
TARGET = "send_to"

# Enter email subject here an edit email below. Avoid using char '/' in subject
SUBJECT = "Very important message!!!!!"

# Change this link to be your webserver's IP Address
LINK = "http://IP-ADDRESS:10008/pixel.png/" + TARGET + "/" + subject_short

curr_time = datetime.datetime.now()
time_string = curr_time.strftime("%m-%d-%Y/%H-%M-%S")

subject_short = SUBJECT.replace(' ', '-')
max_subject_len = 30
if len(subject_short) > max_subject_len:
    subject_short = subject_short[0:max_subject_len-3] + "..."

html = """\
<html>
  <head></head>
  <body>
    <p>Test Email!<br>
       How are you? <br>
       <img src="{0}">
    </p>
  </body>
</html>
"""
## Embed the image link in file
html = html.format(LINK)
print(html)

## Connect to SMTP Server
server = smtplib.SMTP('smtp.gmail.com' , 587)
server.set_debuglevel(1)
server.ehlo()
server.starttls()
server.login(YOU, PASS)

## Message
msg = MIMEMultipart('alternative')
msg['Subject'] = SUBJECT
msg['From'] = YOU
msg['To'] = TARGET
text = ""

## Attach the html and text to the mail
part1 = MIMEText(text, 'plain')
part2 = MIMEText(html, 'html')
msg.attach(part1)
msg.attach(part2)

## Send the mail and disconnect from server
server.sendmail(YOU, TARGET, msg.as_string())
server.quit()

