This is my personal fork of sweethome3D. If you are interested in sweethome3D you really should
use the official version and not this.

# Changes

The fork came about because I wasn't happy with how the program worked in hidpi mode under windows and java 1.10.
That issue was fixed with a change to java3d.
The changes in this repository are mainly refactoring (updating the code to use modern java such as lambdas) and
moving the build system to maven (and this removing most of the external jar files in the repo).

Fork current up until and including 1/9/18

# Building

The project should build out of the box using maven.
Personally I use Intellij IDEA which can load the maven project directly.

