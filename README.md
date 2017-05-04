# HazMat Mathematical Library

## Getting Started

| Item             | Spec                                          |
|:-----------------|:----------------------------------------------|
| Requirements     | Java 8 (to use), sbt 0.13 (to build and test) |
| Current Version  | 1.0.0 (test completed)                        |
| Repository       | https://torao.github.io/math/repo             |
| ID               | at.hazm:hazmat-math:1.0.0                     |

For example to use in `build.sbt`.

```scala
resolvers += "HazMat Math Repository" at "https://torao.github.io/math/repo"

libraryDependencies += "at.hazm" % "hazmat-math" % "1.0.0"
```

## Feature

### SFMT (SIMD-oriented Fast Mersenne Twister) 

SFMT is a Linear Feedbacked Shift Register (LFSR) pseudo-random number generator. See
[http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/index.html](http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/index.html)
for detail.

[SFMTRandom](src/main/java/at/hazm/math/SFMTRandom.java) is a SFMT-based random number generator available as in
standard `java.util.Random` and ported from C implementation with compatibility test.

```java
import at.hazm.math.SFMTRandom;

public class SFMT {
  public void main(String[] args) {
    SFMTRandom random = new SFMTRandom();
    System.out.println(random.nextInt());
    System.out.println(random.nextDouble());
    return;
  }
}
```

## How to Test or Build

This library is implemented in Java, and tested and built in Scala. Please install `sbt` in your environment and run it
on project home.

```
$ sbt package
$ sbt test
```

## License

> MIT License
>   
>   Copyright (c) 2017 Torao Takami
>   
>   Permission is hereby granted, free of charge, to any person obtaining a copy
>   of this software and associated documentation files (the "Software"), to deal
>   in the Software without restriction, including without limitation the rights
>   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
>   copies of the Software, and to permit persons to whom the Software is
>   furnished to do so, subject to the following conditions:
>   
>   The above copyright notice and this permission notice shall be included in all
>   copies or substantial portions of the Software.
>   
>   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
>   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
>   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
>   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
>   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
>   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
>   SOFTWARE.

[LICENSE](/torao/math/LICENSE)