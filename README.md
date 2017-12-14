This desktop java application is designed to download everyday current price for each currency from [http://www.nbp.pl](trusted source).
With enough data it may predict currency market fluctuation.

Project structure:
* common - contains commonly used utilities
* gui - application FrontEnd
* main - catalogue containing main class of this application
* models - catalogue containing objects, used across whole project
* services - interfaces and their implementations, used to connect to DB
* threads - asynchronously executed tasks
    * DownloaderThread - downloads files from trusted source in .xml format (executed once a day, MON-FRI at 4PM)
    * FileProcessingThread - process previously downloaded files and save them as objects to DB (executed every minute MON-FRI, to check for changes)
* resources
    * mapping - hibernate mapping for objects saved to DB
    * persistence.xml - hibernate configuration file
* pom.xml - maven configuration and dependency declaration