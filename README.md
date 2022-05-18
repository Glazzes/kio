## Kio

### About
Kio it's a file manager backend (this project) & mobile app such as Google Drive or
Dropbox where users can store, visualize, share and download
their files at any time.

I realized that the idea of what a folder is, it's abstract, Kio has been made 
as an `Amazon's s3 logic wrapper`, files are stored in s3 through `localstack`, 
this by convenience and because I wanted to get familiar with aws java sdk 
as I do not have a credit card.

### Features
- Crud operations for users
- Crud operations for files and folders
- Copy/Cut files with `RENAME` and `OVERWRITE` strategies
  - `RENAME` will give a new name to those files that collide with the ones on their destination
  - `OVERWRITE`will delete all files in destination that collide with the ones from source

- Copy/Cut folder with strategies `MIX` and `OMIT`, each of these is paired with one
of the Copy/Cut file strategies for a total of 4 possible options

  - `OMIT` will not cut anything if a folder with the same name already exists
  - `MIX` will combine the contents of both folders (including inner folders and its files) recursively

- Share folders and files with other users
    - Sharing a folder will share all of its sub folders as well
- Allow non kio users to see your files (turn your files into public files)
- Create short-lived links for sharing your files and folder without
needing to share your files and folders for long periods of time
- Oauth2 based security

### Tech stack
- Kotlin
- Spring boot
- MongoDB
- Redis
- s3 (localstack)
- React Native (Kio app under development)

I decided to switch from `PostgresDB` to `MongoDB` as it allows for faster reads, Redis is
used to store short-lived sharing links

### Build
Before building remember to meet the requirements for this project
- Docker
- Java 17

Under the docker folder is a `docker-compose.yml`, you can spin up
all the containers with the following command
```
docker-compose -f docker/docker-compose.yml up -d
```