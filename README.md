# Course assignment report

- Student: Antti Koponen
- Student number: Y68225757

### Additional features

- Feature 6: Attach weather information to the danger message
- Feature 7: User can ask danger messages with different search parameters
- Feature 8: User can update user’s own danger messages
- Feature 9: Danger messages can be queried in a specific area

### Comments about the assignment

Now that I'm all done and reading the "Additional Quality Improvements" portion of the project specification, I..

- would've liked to add unit tests for the backend, but they haven't really been covered practically in our studies.
  I've worked with JUnit before, but haven't written any major test suites myself.
- would've definitely liked to add methods for message archival & backups. Unfortunately, I, for some reason, totally
  glossed
  over the quality improvements part, and at this point it's too late to add them. Hopefully, my clean & well-documented
  code
  will make up for it.

I rarely give any feedback on courses, but some constructive criticism for this course would be:

- First off, make the tests better, mostly in the sense of making them clearer. I had to spend a lot of time
  digging in the test suite sources, figuring out what the tests were actually testing and what was failing, especially
  before
  getting the local environment to work and being able to run the tests in a debugger. There was also a lot of
  unnecessary
  information being printed to the console (eg. "./localhost.cer", "200", "sending coords"), which made it very unclear
  what
  was being tested and what failed. Since the tests mainly cover JSON objects, it would be nice to see, for example,
  [JsonUnit](https://github.com/lukas-krecan/JsonUnit) being used in the tests in the future.
- More clear instructions on how to set up the test suite/CI project locally. As a pretty seasoned developer, I still
  had
  trouble getting the HTTPS certificate to work, and I had to fumble around almost an hour before I found an instruction
  somewhere on how to export the certificate from your browser and add it to the project directory.
- I already mentioned this in Slack, but in the future, I'd like to see a newer Java SDK being used, preferably the
  latest
  LTS version available at the time of the course.
- Databases seemed to be a pretty important part of the course, but they haven't really been covered practically in our
  mandatory studies. I would've liked to see more practical examples of how to use and design an efficient (but basic)
  database
  system, keeping in mind things like performance and thread safety (which comes into play as the server uses a thread
  pool).
