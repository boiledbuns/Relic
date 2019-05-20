# Relic

## Updates (May 20th, 2019)
***I'm still in school during summer, so I don't have as much time as I'd like to work on this. I'm going to start a cleanup phase which will freeze feature development for a while***

The focus of this will be on:
1. Code cleanup -> focus on repository methods and packages to:
    - solidify the system currently in place
    - create a more systematic way of handling exceptions
2. Regression -> just need to make sure that:
    - we know expected behaviour (in preparation for adding tests)
    - we can reproduce expected behaviour
3. Adding tests -> focus will be on:
    - setting up ci environment
    - jvm tests for deserializer classes
    - integration tests w/ network requests
    
## Why Relic? (WIP)

Relic is an open source Reddit client developed from the ground up with offline use in mind. 

## About

This is an Android Reddit client I've been working off/on for a few months now. It's a pet project that started as a way to learn new skills (Android dev, mobile design) and has since become a primary focus for me. I try to work on it when I can between school and other stuff I'm learning :)

As of now, the project is in a state of constant flux! I'm still learning new things and trying to balance development between architectural decisions and speed. APIs and features will be changing regularly so I've opted to wait until a more stable base version to add tests.

Once the stable base application with the basic functionality I expect for a Reddit client is reached and sufficient tests have been added, I will release it as v1. From there, Contributions will be opened and I will be actively working to introduce new features and a better experience for app users.

I have a lot of ideas that I want to implement, but my primary focus for now is to implement basic client functionality. I'm also going to be spending a lot more time tidying up documentation in the next few days

Consult the [wiki](https://github.com/13ABEL/Relic/wiki) for more info about [contributing](https://github.com/13ABEL/Relic/wiki/Contribution-Quickstart)

## roadmap to v1 milestone

- [x] display individual subreddits
- [x] display individual posts
- [x] display individual users
- [x] display user previews
- [x] display user previews
- [x] setup the app to handle reddit links
- [x] basic media handling (ie. imgur, gifs, v.reddit, etc)
- [ ] create new posts
- [ ] create new comments
- [ ] search for subs, posts, and users
- [ ] background post retrieval while app is inactive

## additional features after v1

- [ ] better media handling (modules)
- [ ] allow custom theme creation and import
- [ ] post and comment filtering

## documentation roadmap

1. Add a folder for decisions and architecture overview
2. Make a list of libraries and tech being used
