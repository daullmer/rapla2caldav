# Rapla2CalDav
This is a project to automatically sync a Rapla schedule to a CalDav calendar.
It even checks if the schedule changed if you run this project periodically!

## Usage
The easiest way to run this project periodically is with a Docker container (if you study in BW maybe in the BW-Cloud).
In order to do that you have to create a file containing all the required environment variables. You can see an example [here](env.EXAMPLE).

The DHBW Stuttgart is hosting an OwnCloud which you could use as a CalDav instance. To use it, create a new calendar in your OwnCloud, click the three dots and copy your private URL. Put it in your environment file. Username and Password are your normal DHBW credentials.
To get the Rapla Link, copy the key inside the URL provided to you by the secretary.

If you have created a file with the environment variables, you can run the container like this:

```docker run --name rapla2caldav --env-file env.secrets ghcr.io/daullmer/rapla2caldav```

The container will start and do an initial sync. To run in periodically, you can start the container every hour with the following cron job:

```0 7-19 * * *    username docker start rapla2caldav```

It will start the container every hour between 7 and 19 o'clock. Outside these hours, it is very unlikely that your schedule will change.

## Get a sharable URL of the calendar
In the OwnCloud calendar UI, click on the share icon next to your calendar. Then click the plus and the three dots and select "Abonnement-Link copieren". You can add this URL in your phone calendar or share it publicly with your class.
[This](https://www.techrepublic.com/article/how-to-subscribe-to-and-manage-public-calendars-in-ios-15/) is a handy example of how to add it to your iPhone.