# Relic

## Android Reddit Client
This is an Android Reddit client I've been working on for a few months now. I started this project with the intention of improving my skills as a mobile developer and designer

## project roadmap - features
- [x] display individual subreddits
- [x] display individual posts
- [x] display individual users
- [x] display user previews
- [x] setup the app to handle reddit links
- [ ] better media handling (ie. imgur, gifs, v.reddit, etc)
- [ ] searching for subs, posts, and users
- [ ] background post retrieval
- [ ] allow custom theme creation and import
- [ ] post and comment filtering

## project roadmap - maintenance, cleanup, and performance
- [ ] speed up recyclerviews (ie. currently drops frame)
- [ ] replace global scope with per view scoping for coroutines

## Development setup
1. Clone the repo
2. Open the project in android studio and add an new resource file called "secrets"
3. Add a new string with name: "client_id"
4. Add your api key as the resource value

## List of Todo's off the top of my head
1. Add a folder for decisions and architecture overview
2. Make a list of libraries and tech being used