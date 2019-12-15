[![CircleCI](https://circleci.com/gh/13ABEL/Relic.svg?style=svg)](https://circleci.com/gh/13ABEL/Relic)
# Relic

The project is currently a WIP. Updates and progress can be found [here](./docs/updates.md)

## Why Relic? (WIP)

Relic is an open source Reddit client developed from the ground up with offline use in mind.

## About

This is an Android Reddit client I've been working off/on for a few months now. It's a pet project that started as a way to learn new skills (Android dev, mobile design) and has since become a primary focus for me. I try to work on it when I can between school and other stuff I'm learning :)

As of now, the project is in a state of constant flux! I'm still learning new things and trying to balance development between architectural decisions and speed. APIs and features will be changing regularly so I've opted to wait until a more stable base version to add tests. For the same reason, I will be pushing my commits directly to master and **NOT** keeping my changes tidy. I've considered the options and opted for this because I will be the only person working on the project until V1 and I want to maintain my velocity.

Once the stable base application with the basic functionality I expect for a Reddit client is reached and sufficient tests have been added, I will release it as v1. From there, expect clean contributions with pull requests and tight changes. Contributions will also be opened and I will be actively working to introduce new features and a better experience for app users.

I have a lot of ideas that I want to implement, but my primary focus for now is to implement basic client functionality. I'm also going to be spending a lot more time tidying up documentation in the next few days

Consult the [wiki](https://github.com/13ABEL/Relic/wiki) for more info about [contributing](https://github.com/13ABEL/Relic/wiki/Contribution-Quickstart)

## Screenshots
Here are some screenshots showing off what relic looks like in different situations. Although These definitely don't show off everything relic has to offer, they should give you an idea of what the app looks like

|||||
|---|---|---|---|
| **overview of a current user** | **overview of subreddits** | **subreddit with primarily text-base content** | **subreddit with primarily image-base content** |
| ![](https://i.imgur.com/XVvXmOk.png) | ![](https://i.imgur.com/iOeXGXC.png) | ![](https://i.imgur.com/5glGf8F.png) | ![](https://i.imgur.com/BshKB1a.png)|
| **expanded full text post** | **comment section of post** | **user preview panel** | **prototype image viewer** |
| ![](https://i.imgur.com/foRqxeu.png) | ![](https://i.imgur.com/E2nLVbK.png) | ![](https://i.imgur.com/b6Yy670.png)| ![](https://i.imgur.com/6Rsokz6.png) |


## roadmap to v1 milestone

- [x] display individual subreddits
- [x] display individual posts
- [x] display individual users
- [x] display user previews
- [x] display user previews
- [x] setup the app to handle reddit links
- [x] basic media handling (ie. imgur, gifs, v.reddit, etc)
- [x] create new posts
- [x] create new comments
- [ ] handle unauthenticated users
- [x] search for subs, posts, and users
- [x] background post retrieval while app is inactive

## additional features after v1

- [ ] better media handling (modules)
- [ ] allow custom theme creation and import
- [ ] post and comment filtering

## documentation roadmap

1. Add a folder for decisions and architecture overview
2. Make a list of libraries and tech being used