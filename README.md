# MaterialShowcaseView

__Note: this is a fork of a fork of a ... so this readme might not reflect the actual&current info, and I don't have time to thoroughly update it. Read with a grain of salt.__

A Material Design themed ShowcaseView for Android

This library is heavily inspired by the original [ShowcaseView library][1].

Since Google introduced the Material design philosophy I have seen quite a few apps with a nice clean, flat showcase view (the Youtube app is a good example). The only library out there however is the [original one][1]. This was a great library for a long time but the theming is now looking a bit dated.

About this fork: the original implementation didn't have a skip button. As some users may find frustrating to be stuck in a tutorial sequence I've created a new button that enabled user to skip the remaining items.

I've also added the option to use a Button instead of a TextView to skip (default is still TextView). 


About _this_ fork: The original didn't have the option to show oval around target, which is now implemented.

![Logo](http://i.imgur.com/QIMYRJh.png)

![Animation][2]

# Gradle
--------

[![jitpack][4]][5]

Add the jitpack repo to your your project's build.gradle at the end of repositories [Why?](#why-jitpack)

/build.gradle
```groovy
allprojects {
	repositories {
		jcenter()
		maven { url "https://jitpack.io" }
	}
}
```

Then add the dependency to your module's build.gradle:

/app/build.gradle
```groovy
compile 'com.github.luq-0:MaterialShowcaseView:1.2.0'
```

NOTE: Some people have mentioned that they needed to add the @aar suffix to get it to resolve from JitPack:
```groovy
compile 'com.github.fibelatti:MaterialShowcaseView:1.2.0@aar'
```

# How to use
--------
This is the basic usage of a single showcase view, you should check out the sample app for more advanced usage.

```java

	// single example
	new MaterialShowcaseView.Builder(this)
                .setTarget(mButtonShow)
                .setDismissText("GOT IT")
                .setContentText("This is some amazing feature you should know about")
                .setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .show();

    // single example with button
    new MaterialShowcaseView.Builder(this)
                .setTarget(mButtonShow)
                .withButtonDismissStyle()
                .withWhiteDismissButton()
                .setDismissTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setDismissText("GOT IT")
                .setContentText("This is some amazing feature you should know about")
                .setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .show();

	// sequence example            
	ShowcaseConfig config = new ShowcaseConfig();
    config.setDelay(500); // half second between each showcase view

    MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

    sequence.setConfig(config);

    sequence.addSequenceItem(mButtonOne,
            "This is button one", "GOT IT", "SKIP");

    sequence.addSequenceItem(mButtonTwo,
            "This is button two", "GOT IT"), "SKIP";

    sequence.addSequenceItem(mButtonThree,
            "This is button three", "GOT IT", "SKIP");

    sequence.start();
                
```

# Button styles
--------

If you decided to use a Button to skip each tutorial by calling `.withButtonDismissStyle()` there are 21 themes that you can use (the same color palette as provided by [Material Palette](https://www.materialpalette.com/)):

- `.withWhiteDismissButton()`
- `.withBlackDismissButton()`
- `.withRedDismissButton()`
- `.withPinkDismissButton()`
- `.withPurpleDismissButton()`
- `.withDeepPurpleDismissButton()`
- `.withIndigoDismissButton()`
- `.withBlueDismissButton()`
- `.withLightBlueDismissButton()`
- `.withCyanDismissButton()`
- `.withTealDismissButton()`
- `.withGreenDismissButton()`
- `.withLightGreenDismissButton()`
- `.withLimeDismissButton()`
- `.withYellowDismissButton()`
- `.withAmberDismissButton()`
- `.withOrangeDismissButton()`
- `.withDeepOrangeDismissButton()`
- `.withBrownDismissButton()`
- `.withGrayDismissButton()`
- `.withBlueGrayDismissButton()`

# Why Jitpack
------------
Publishing libraries to Maven is a chore that takes time and effort. Jitpack.io allows me to release without ever leaving GitHub so I can release easily and more often.

# License
-------

    Copyright 2015 Dean Wild

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: https://github.com/amlcurran/ShowcaseView
[2]: http://i.imgur.com/rFHENgz.gif
[3]: https://code.google.com/p/android-flowtextview/
[4]: https://jitpack.io/v/fibelatti/MaterialShowcaseView.svg
[5]: https://jitpack.io/#fibelatti/MaterialShowcaseView
