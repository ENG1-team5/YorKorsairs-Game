
# York Pirates!

This repo contains the game for the York Pirates! Team 10 ENG1 group project taken over by Team 5.

Website Repo: https://github.com/ENG1-team5/ENG1-team5.github.io
Website Link: https://ENG1-team5.github.io

## Download and build 

0. Install Temurin 11 from https://adoptium.net/

    Ignore this step if you have Java installed.

    If you get an issue similar to `Incompatable major version 61` then you need to make sure that you installed Temurin 11

1. Pull this repository using:

    `git pull https://github.com/ENG1-team5/YorKorsairs-Game.git`

2. Create a new branch

    `git checkout <branch_name>`

3. Make any changes to the code

4. Test your changes by running the program

    `gradlew.bat desktop:run`

5. Commit and push your changes. Using VSCode or some other git assistant is reccomended

### Creating an executable jar file

Note that CI should make this irrelevant

1. execute `gradlew.bat dist`

2. Locate file in desktop/build/libs/desktop-1.0.jar

## CI

Coming soon
