# README #

App to automate the bird counts at [Jordsand Organization](https://www.jordsand.de/). However it now
has a much broader audience.

Jordsand takes care of many nature sanctuaries which are of special interest for many bird species.
Therefore all occurring species are being count regularly. The app should automate this process.

**Warning** not all features described here are necessarily available yet.
 See [Roadmap](#roadmap) for details

## Vision ##

Although there are many - really good - monitoring apps out there, they all follow a quite universal
approach: everybody should be able to track every animal (or bird, amphibian, plant ...) everywhere.
Usually such an app offers a map which shows your current position (using GPS and an online map service)
and then allows you to add a new sighting from a list of every bird (reptile, plant, butterfly ...)
known.

This app takes a different and much more narrow approach: it should not be used by everyone, but rather
by the employees and activists of one specific conservation area. It does not display a map of
(potentially) everywhere, instead it just shows a map of the natural reserve the users work in.
And last but not least, the app is not primarily targeted at tracking every species out there,
but rather offers a "small" list of target species which can be encountered in the area most likely
(other species may be added manually tho). It is therefore tailored to one specific nature sanctuary
and one specific set of management indicator species.

## Adapting to your nature reserve ##

As the app is developed with easy adaption to different areas as one big design principle, all
the data specific to your wildlife reserve only needs to be put into one XML file and imported by
the app. An GUI for creating such a description will be created at one point as well, so that
non-computer scientists may tailor the app for their area as well.

## Roadmap ##

These are the things that will be implemented next:

1. A map of the reserve area
2. Export observations
3. Enable import of new specifications (through their XML file) and offline-maps

## Contact and reporting bugs ##

Fell free to contact me via [email](mailto:portux@posteo.de) or just
[open an issue](https://github.com/portux/Jordsand-Birdcount/issues/new) yourself.
I'll try to fix it as soon as possible, however depending on my resources this may still take some
time.
