Please find attached working code written in java and using redis. Please follow below steps.

1- Import project in InteliJ IDE or Eclipse and and run the main file.
2- Run this url in browser - http://localhost:8080/api/pdf/download
3- This will download the file in browser, I have taken few assumptions so that you can just click URL and get the output so that set dummy data for user and Questions, Just focused on provided module that is PDF generation
4- Inactive user has been from expired session from using session registry.

PDF generator for questions which is search by a user on the web/app but in below mentioned class few assumptions has taken care of those are :
1- This controller(end point) just taking care of PDF generation with its data
2- Here PDF data is set by method (getDataObjectList) manually for single user only, In real world we can get all users which are inactive in last 5 mins from db (mysql | Redis) where questions data is stored
3- Generated files are permanently saved on the  server in PDF_Reports folder and also downloadable in browser too
5- There are 3 options to manage 5 mins delay time
a) We can identify user inactivity on basis of user session and heartbeat api call send to the the system
c) We can write a script(cron) which get all users which are inactive since last 5 mins and generate PDF for all users  and send  that to queue for sending email and sms whatever is required .I used this #2 approach and written code for single user only

Let me know in case of any issue.
-- Rahul Chaurasia
