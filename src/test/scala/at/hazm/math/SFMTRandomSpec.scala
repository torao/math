package at.hazm.math

import org.specs2.Specification

class SFMTRandomSpec extends Specification {
  def is =
    s2"""
       |
       |generate random int and long    $e0
       |generate 32bit random array     $e1
       |generate 64bit random array     $e2
       |integer random to double        $e3
       |generate same random by next    $e4
      """.stripMargin

  def ex = {
    val BLOCK_SIZE = 100000
    val BLOCK_SIZE64 = 50000
    val COUNT = 1000

    val array1 = new Array[SFMTRandom.W128T](BLOCK_SIZE / 4)
    val array2 = new Array[SFMTRandom.W128T](10000 / 4)

    val array32 = new Array[Int](10000)
    val array32_2 = new Array[Int](10000)

    val sfmt = new SFMTRandom(SFMTParam.P19937)
    if(sfmt.get_min_array_size32() > 10000) {
      printf("array size too small!\n")
      failure
    }
    println(sfmt.getId)
    println("32 bit generated randoms")
    println("init_gen_rand__________")
    /* 32 bit generation */
    sfmt.init_gen_rand(1234)
    sfmt.fill_array32(array32)
    sfmt.fill_array32(array32_2)
    sfmt.init_gen_rand(1234)
    for(i <- 0 until 10000) {
      if(i < 1000) {
        print(f"${array32(i).toLong & 0xFFFFFFFFL}%10d ")
        if(i % 5 == 4) {
          println()
        }
      }
    }
    success
  }

  def e0 = {
    val random = new SFMTRandom(SFMTParam.P19937)
    random.init_gen_rand(0)
    random.nextInt()
    random.init_gen_rand(0)
    random.nextLong()
    success
  }

  def e1 = {
    Seq(
      0 -> Seq(772581976L, 265233418L, 1048142482L, 1602309670L, 3373935053L,
        3413705399L, 3210284462L, 863469473L, 3245518687L, 498391845L),
      2147483647 -> Seq(904772199L, 1064192912L, 1906522326L, 4285143873L, 547006276L,
        44221428L, 2785557915L, 3086200219L, 3882166955L, 1307696526L),
      -2147483648 -> Seq(3802665797L, 2348422453L, 77172185L, 2594230312L, 3860939425L,
        3189465778L, 2298123692L, 2355150791L, 1308024730L, 4017304374L),
      1234 -> Seq(3440181298L, 1564997079L, 1510669302L, 2930277156L, 1452439940L,
        3796268453L, 423124208L, 2143818589L, 3827219408L, 2987036003L)
    ).flatMap { case (seed, expected) =>
      val random = new SFMTRandom(SFMTParam.P19937)
      val actual = new Array[Int](random.get_min_array_size32())
      random.init_gen_rand(seed)
      random.fill_array32(actual)
      expected.zip(actual).zipWithIndex.map { case ((a, b), i) => s"$i:${a.toInt}" === s"$i:$b" }
    }.reduceLeft(_ and _)
  }

  def e2 = {
    Seq(
      0 -> Seq(BigInt("1139168856888879704"), BigInt("6881867631762694802"),
        BigInt("14661753050257566157"), BigInt("3708573150839639470"),
        BigInt("2140576678113619807"), BigInt("6346274159271671697"),
        BigInt("9960282081407665467"), BigInt("13610290706503721531"),
        BigInt("3045346872446303051"), BigInt("938620360515019110")),
      2147483647 -> Seq(BigInt("4570673754579778151"), BigInt("18404552795096299734"),
        BigInt("189929587589424964"), BigInt("13255129012298595739"),
        BigInt("5616513816144980651"), BigInt("6280535848713548861"),
        BigInt("4930669209649472364"), BigInt("9999167623858369312"),
        BigInt("15484002769673203704"), BigInt("18123196381854081846")),
      -2147483648 -> Seq(BigInt("10086397636629762885"), BigInt("11142134348409048537"),
        BigInt("13698651212082135713"), BigInt("10115295626791654828"),
        BigInt("17254190905715777434"), BigInt("6451077998237488581"),
        BigInt("12855726226210285943"), BigInt("675183718663119041"),
        BigInt("2312072302283282476"), BigInt("5290760732985564704")),
      1234 -> Seq(BigInt("6721611276080709682"), BigInt("12585444554746559478"),
        BigInt("16304848853923953028"), BigInt("9207630728734989552"),
        BigInt("12829221948686777296"), BigInt("6600688339407400754"),
        BigInt("10887302133822135185"), BigInt("2342338980101906157"),
        BigInt("5886399791876835913"), BigInt("17157548324798079420"))
    ).flatMap { case (seed, expected) =>
      val random = new SFMTRandom(SFMTParam.P19937)
      val actual = new Array[Long](random.get_min_array_size64())
      random.init_gen_rand(seed)
      random.fill_array64(actual)
      expected.zip(actual).zipWithIndex.map { case ((a, b), i) => s"$i:${a.toLong}" === s"$i:$b" }
    }.reduceLeft(_ and _)
  }

  def e3 = {
    val Epsilon = math.pow(2, 1 - 53)
    (SFMTRandom.to_real1(Int.MinValue) === 0.0) and
      (math.abs(SFMTRandom.to_real1(0) - math.abs(Int.MinValue / 0xFFFFFFFFL.toDouble)) must be_<=(Epsilon)) and
      (SFMTRandom.to_real1(Int.MaxValue) === 1.0) and
      // ---
      (SFMTRandom.to_real2(Int.MinValue) === 0.0) and
      (math.abs(SFMTRandom.to_real2(0) - math.abs(Int.MinValue / 0x100000000L.toDouble)) must be_<=(Epsilon)) and
      (SFMTRandom.to_real2(Int.MaxValue) must be_<(1.0)) and
      // ---
      (SFMTRandom.to_real3(Int.MinValue) must be_>(0.0)) and
      (math.abs(SFMTRandom.to_real2(0) - math.abs(Int.MinValue / 0x100000000L.toDouble)) must be_<=(Epsilon)) and
      (SFMTRandom.to_real3(Int.MaxValue) must be_<(1.0))
  }

  def e4 = {
    val random1 = new SFMTRandom(SFMTParam.P19937)
    val random2 = new SFMTRandom(SFMTParam.P19937)
    Seq(Int.MinValue, 0, Int.MaxValue, 1234).flatMap{ seed =>
      {
        val expected = new Array[Int](random1.get_min_array_size32())
        random1.init_gen_rand(seed)
        random2.init_gen_rand(seed)
        (1 to 10).flatMap{ _ =>
          random1.fill_array32(expected)
          expected.map{ e =>
            e === random2.nextInt()
          }
        }
      } ++ {
        val expected = new Array[Long](random1.get_min_array_size64())
        random1.init_gen_rand(seed)
        random2.init_gen_rand(seed)
        (1 to 10).flatMap{ i =>
          random1.fill_array64(expected)
          expected.map{ e =>
            s"$seed:$i:$e" === s"$seed:$i:${random2.nextLong()}"
          }
        }
      }
    }.reduceLeft(_ and _)
  }

}
