Bela blok MiVi
==============
Android application for storing results in four player Bela card game.

Download
--------
Available on [Play Store][1]

Architecture
------------
This project implements clean architecture. Data model can be found in bela.mi.vi.data package and it describes properties of Player, Game, Set and Match. Also it contains repository and settings interface. Business logic can be found in bela.mi.vi.interactor package and it provides all actions that are available with data model. Both of those packages represent center part of architecture and do not depend on Android.
Android implementation of data sources uses Room (Sqlite database) and it can be found in bela.mi.vi.android.room package.
UI part is implemented as single activity utilizing Android Navigation library. Screens (Fragments) contains ViewModels which are injected using Android Hilt dependency injection library. ViewModels and in some cases Fragments use injected interactors to perform actions over data models.

Author
------
Damir Mihaljinec - @dmihaljinec on GitHub

License
-------
GPLv3. See the [LICENSE][2] file for details.

[1]: https://play.google.com/store/apps/details?id=bela.mi.vi.blok
[2]: https://github.com/dmihaljinec/BelaMiVi/blob/master/LICENSE.md
