# Updates

This is just a file for keeping track of progress and anything that comes up.

## June 4th, 2019

Alot of the code has been cleaned and tidied up. There's working code hidden beneath API's that I'd like to refactor later, but I won't touch it now because it works. I've realized that I've spent too much time refactoring and redesigning different parts of the app which has led to extremely slow progress. I'm going to try to shift my approach and try to prototype rapidly to implement more features.

## May 20th, 2019

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
