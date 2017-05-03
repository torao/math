package at.hazm.math

import org.specs2.Specification

class SFMTRandomSpec extends Specification {
  def is =
    s2"""
       |$ex
       |generate random int and long    $e0
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
    println(sfmt.getId())
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
    println(random.nextInt())
    println(random.nextLong())
    success
  }

}
