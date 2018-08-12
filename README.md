# Revolut Test Task

Published to GitHub as was required in the task.

## Build

`sh gradlew build` or `gradlew.bat build`

As a repository for resolve dependencies build script will use [JCenter](https://bintray.com/bintray/jcenter).

## Run

`java -jar revolut-test-task-1.0-SNAPSHOT.jar`

Jar will be located in `build/libs`

## Description

User can have only one account per each currency. If transfer is done between account with different currencies, money will be automatically exchanged with a provided rate. 
Exchange rate provided only for EUR/USD and USD/EUR. 

## API

User

`GET /user` get all users

`GET /user/:id` get a user by id
 
`POST /user` create a new user 

`DELETE /user/:id` delete a user by id

Account

`GET /user/:userId/account` get all accounts from provided user

`GET /account/:accountId` get an account by id

`POST /user/:userId/account` create anew account for a provided user
  body _required_ `{"currency": "EUR"}`
  Currency can be: EUR, USD, GBP, RUB.

`PUT /account/:accountId/topUp` add money to an account
  body _required_ `{"amount": 10.15}`

`PUT /account/:accountId/withdraw`
  body _required_ `{"amount": 10.15}`
  Status 400 could be if the account has not enough money.
  
`DELETE /account/:accountId` withdraw money from an account

`PUT /account/:accountId/transfer` transfer money a provided account
  body _required_ `{"account": 1, "amount": 10.15}`
  where `account` is an id of an account for which money should be transferred. Could be from the same user.
  Status 400 could be if the account has not enough money.

 