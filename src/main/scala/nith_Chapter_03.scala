import scala.annotation.tailrec
import util.log

sealed trait List[+A]

//case object Nil extends List[Nothing]
//
//case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {

  case object Nil extends List[Nothing]

  case class Cons[+A](head: A, tail: List[A]) extends List[A]

  def apply[A](as: A*): List[A] = {
    @tailrec
    def go(l: List[A], as: A*): List[A] =
      if (as.isEmpty) l
      else go(Cons(as.head, l), as.tail: _*)
    go(Nil, as.reverse: _*)
  }


  //exercise 3.2
  // Implement the function tail for removing the first element of a List. Note that the
  // function takes constant time. What are different choices you could make in your
  // implementation if the List is Nil? We’ll return to this question in the next chapter.
  def head[A](l: List[A]): A = l match {
    case Cons(x, t) => x
  }

  def tail[A](l: List[A]): List[A] = l match {
    case Nil => Nil
    case Cons(x, t) => t
  }

  //exercise 3.3
  def setHead[A](l: List[A], a: A): List[A] = l match {
    case Nil => Nil
    case Cons(h, t) => Cons(a, t)
  }

  //exercise 3.4
  @tailrec
  def drop[A](as: List[A], n: Int): List[A] = as match {
    case Nil => Nil
    case _ if n < 1 => as
    case Cons(x, t) => drop(t, n - 1)
  }

  //exercise 3.5
  @tailrec
  def dropWhile[A](l: List[A])(f: A => Boolean): List[A] = l match {
    case Cons(x, t) if f(x) => dropWhile(t)(f)
    case _ => l
  }

  //exercise 3.6
  def init[A](l: List[A]): List[A] = l match {
    case Nil => Nil
    case Cons(x, Nil) => Nil
    case Cons(x, t) => Cons(x, init(t))
  }

  //exercise 3.7
  def foldRight[A, B](as: List[A], z: B)(f: A => (=> B) => B): B = {
    //  log("foldRight(%s)".format(as + "," + z))
    as match {
      case Nil => z
      case Cons(x, xs) => f(x)(foldRight(xs, z)(f))
    }
  }

  def sum(l: List[Int]): Int = {
    @tailrec
    def go(l: List[Int], n: Int): Int = l match {
      case Nil => n
      case Cons(x, xs) => go(xs, x + n)
    }
    go(l, 0)
  }

  def product(l: List[Int]): Int = {
    @tailrec
    def go(l: List[Int], n: Int): Int = l match {
      case Nil => n
      case Cons(x, xs) => go(xs, x * n)
    }
    go(l, 1)
  }

  def sumFoldRight(l: List[Int]) = foldRight(l, 0)(x => y => x + y)

  def productFoldRight(l: List[Int]) = foldRight(l, 1)(x => if (x == 0) _ => 0 else y => x * y)

  // end of exercise 3.7

  //exercise 3.10
  // first B parameter B is meant to be referenced by name instead of value
  // for lazy evaluation, so (=>B) instead of B
  @tailrec
  def foldLeft[A, B](as: List[A], z: B)(f: A => (=> B) => B): B = {
    // log("foldLeft(%s)".format(as + "," + z))
    as match {
      case Nil => z
      case Cons(x, xs) => foldLeft(xs, f(x)(z))(f)
    }
  }

  //exercise 3.11
  def sumFoldLeft(l: List[Int]) = foldLeft(l, 0)(x => y => x + y)

  def productFoldLeft(l: List[Int]) = foldLeft(l, 1)(x => if (x == 0) _ => 0 else y => x * y)

  def max[A](as: List[A])(order: A => A => Boolean)(bottom: A) = foldLeft(as, bottom)(a => interim => if (order(a)(interim)) interim else a)

  def max(ints: List[Int]) = max[Int](ints)(i => j => i < j)(Int.MinValue)

  def length[A](as: List[A]): Int = foldLeft(as, 0)(a => y => 1 + y)

  //exercise 3.12
  def reverse[A](as: List[A]): List[A] = foldLeft(as, Nil: List[A])(a => l => Cons(a, l))

  //exercise 3.14
  def append[A](l1: List[A], l2: List[A]): List[A] = foldLeft(reverse(l1), l2)(a => as => Cons(a, as))

  //exercise 3.15
  def concat[A](ass: List[List[A]]): List[A] = ass match {
    case Nil => Nil
    case Cons(h, t) => append(h, concat(t))
  }

  //exercise 3.17
  def myString[A](as: List[A]): String = {
    @tailrec
    def go(as: List[A], s: String): String = {
      val sAppend: String = if (s == "") "" else s + ","
      as match {
        case Nil => sAppend
        case Cons(h, t) => {
          lazy val hString: String = h match {
            case x@Nil => "()"
            case x@Cons(_, _) => myString(x)
            case x => x.toString
          }
          if (t == Nil) sAppend + hString else go(t, sAppend + hString)
        }
      }
    }
    "List(" + go(as, "") + ")"
  }

  def myString[A](asPair: (List[A], List[A])): String = "(" + myString(asPair._1) + "," + myString(asPair._2) + ")"

  //  def myString[A](ass: List[List[A]]): String = foldLeft[List[A],String](ass,"()")(as => s => s + "\n" + myString(as))

  //exercise 3.18
  def map[A, B](as: List[A])(f: A => B): List[B] = reverse(foldLeft[A, List[B]](as, Nil)(a => bs => Cons(f(a), bs)))

  //exercise 3.19
  def filter[A](as: List[A])(f: A => Boolean): List[A] = {
    log("...filter( " + List.myString(as) + " )( " + f.toString + " )")
    reverse(foldLeft[A, List[A]](as, Nil)(a => l => if (f(a)) Cons(a, l) else l))
  }

  //exercise 3.20
  def flatMap[A, B](as: List[A])(f: A => List[B]): List[B] = reverse(foldLeft[A, List[B]](as, Nil)(a => bs => append(f(a), bs)))

  //exercise 3.21
  def flatFilter[A](as: List[A])(f: A => Boolean): List[A] = flatMap[A, A](as)(a => if (f(a)) List(a) else Nil)

  //exercise 3.23: Pair and zipWith
  def Pair[A, B](as: List[A])(bs: List[B]): List[(A, B)] = (as, bs) match {
    case (_, Nil) => Nil
    case (Nil, _) => Nil
    case (Cons(a, ta), Cons(b, tb)) => Cons((a, b), Pair(ta)(tb))
  }

  def zipWith[A, B, C](as: List[A])(bs: List[B])(f: A => B => C): List[C] =
    reverse(foldLeft[Tuple2[A, B], List[C]](List.Pair(as)(bs), Nil)(x => l => Cons((f(x._1)(x._2)), l)))

  //exercise 3.24: isInitialSegment and hasSubsequence
  @tailrec
  def isInitialSegment[A](ini: List[A], as: List[A]): Boolean = ini match {
    case Nil => true
    case Cons(i, iniTail) => as match {
      case Nil => false
      case Cons(h, t) if i == h => isInitialSegment(iniTail, t)
      case _ => false
    }
  }

  @tailrec
  def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean = sub match {
    case Nil => true
    case Cons(a, as) => List.dropWhile(sup)(x => x != a) match {
      case Nil => false
      case Cons(a1, Nil) => as == Nil
      case Cons(a1, Cons(a2, t)) => isInitialSegment(as, Cons(a2, t)) || hasSubsequence(Cons(a2, t), sub)
    }
  }


  final def exists[A](as: List[A])(p: A => Boolean): Boolean = as match {
    case Cons(h, t) => p(h) || exists(t)(p)
    case _ => false
  }

  // needed for chapter 6
  def fill[A](n: Int)(a: A): List[A] = {
    @tailrec
    def go(n: Int)(intermediaryResult: List[A]): List[A] = if (n < 1) intermediaryResult else go(n - 1)(Cons(a, intermediaryResult))
    go(n)(Nil)
  }


  // needed for chapter 7
  final def take[A](as: List[A])(n: Int): List[A] = as match {
    case Nil => Nil
    case Cons(h, t) if (n < 1) => Nil
    case Cons(h, t) if (n > 0) => Cons(h, take(t)(n - 1))
  }

  //    @tailrec
  final def shovel[A](as: List[A])(bs: List[A])(n: Int): (List[A], List[A]) = (drop(as, n), append(take(as)(n), bs))

  def split[A](as: List[A])(n: Int): (List[A], List[A]) = shovel[A](as)(Nil)(n)

  def halve[A](as: List[A]): (List[A], List[A]) = split(as)(length(as) / 2)

  // needed for chapter 8
  final def integers(from: Int)(to: Int): List[Int] = {
    @tailrec
    def go(ints: List[Int])(from: Int)(to: Int): List[Int] = if (from == to) Cons(from, ints) else go(Cons(from, ints))(from + (to - from).signum)(to)
    List.reverse(go(Nil)(from)(to))
  }

  final def merge[A](left: List[A])(right: List[A])(order: A => A => Boolean): List[A] = {
    @tailrec
    def go(intermedResult: List[A])(left: List[A])(right: List[A])(order: A => A => Boolean): List[A] = left match {
      case Nil => List.append[A](List.reverse(intermedResult), right)
      case Cons(l, lTail) => right match {
        case Nil => List.append[A](List.reverse(intermedResult), left)
        case Cons(r, rTail) => if (order(l)(r)) go(Cons(l, intermedResult))(lTail)(right)(order) else go(Cons(r, intermedResult))(left)(rTail)(order)
      }
    }
    go(Nil)(left)(right)(order)
  }

  final def merge[A](ass: List[List[A]])(order: A => A => Boolean): List[A] = List.foldLeft[List[A], List[A]](ass, Nil)(as1 => as2 => merge[A](as1)(as2)(order))

  final def splitIntoReversedListOfSingletons[A](as: List[A]): List[List[A]] = List.foldLeft[A, List[List[A]]](as, Nil)(a => ass => Cons(Cons(a, Nil), ass))

  def mergeSort[A](as: List[A])(order: A => A => Boolean): List[A] = merge[A](splitIntoReversedListOfSingletons[A](as))(order)

  final def mergeSort(ints: List[Int]): List[Int] = mergeSort[Int](ints)(i => j => i <= j)

  final def isSortedMax[A](as: List[A])(order: A => A => Boolean)(z: (Boolean, A)): (Boolean, A) = as match {
    case Cons(a, aTail) if z._1 => isSortedMax[A](aTail)(order)((order(z._2)(a), a))
    case _ => z
  }

  def isSorted[A](as: List[A])(order: A => A => Boolean): Boolean = as match {
    case Nil => true
    case Cons(a1, aTail) => isSortedMax(aTail)(order)((true, a1))._1
  }

  def isSorted(ints: List[Int]): Boolean = isSorted[Int](ints)(i => j => i <= j)

}

sealed trait Tree[+A]

case class Leaf[A](value: A) extends Tree[A]

case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree {

  def fold[A, B](as: Tree[A])(g: A => B)(f: (=> B) => (=> B) => B): B = as match {
    case Leaf(a) => g(a)
    case Branch(l, r) => f(fold(l)(g)(f))(fold(r)(g)(f))
  }

  //exercise 3.25
  def size[A](as: Tree[A]): Int = as match {
    case Leaf(a) => 1
    case Branch(l, r) => size(l) + size(r) + 1
  }

  def sizeFold[A](as: Tree[A]): Int = fold(as)(a => 1)(x => y => x + y + 1)

  //exercise 3.26
  def maxVal(as: Tree[Int]): Int = fold[Int, Int](as)(i => i)(x => y => x.max(y))

  //exercise 3.27
  def depth[A](as: Tree[A]): Int = fold[A, Int](as)(i => 0)(x => y => 1 + x.max(y))

  //exercise 3.28
  def map[A, B](as: Tree[A])(h: A => B): Tree[B] = fold[A, Tree[B]](as)(a => Leaf(h(a)))(l => r => Branch(l, r))

  // stringLength transforms a tree of strings into a tree containing the length of each string
  def stringLength(strings: Tree[String]): Tree[Int] = map[String, Int](strings)(s => s.length)
}


sealed trait FinTree[+A]

case class fTree[A](value: A, ts: List[FinTree[A]]) extends FinTree[A]

object FinTree {

  import List.{Nil, Cons}

  def branches[A](t: FinTree[A]): List[FinTree[A]] = t match {
    case fTree(value, ts) => ts
  }

  def rootValue[A](t: FinTree[A]): A = t match {
    case fTree(value, ts) => value
  }

  //exercise 3.25
  def size[A](t: FinTree[A]): Int = List.foldLeft[FinTree[A], Int](FinTree.branches(t), 1)(x => i => i + FinTree.size[A](x))

  //exercise 3.26
  def maxVal(t: FinTree[Int]): Int = List.foldLeft[FinTree[Int], Int](FinTree.branches(t), FinTree.rootValue(t))(x => i => i.max(FinTree.maxVal(x)))

  //exercise 3.27
  def depth[A](t: FinTree[A]): Int = List.foldLeft[FinTree[A], Int](FinTree.branches(t), 0)(x => i => i.max(1 + FinTree.depth[A](x)))

  //exercise 3.28
  def map[A, B](t: FinTree[A])(h: A => B): FinTree[B] =
    List.foldLeft[FinTree[A], FinTree[B]](FinTree.branches(t), fTree(h(FinTree.rootValue(t)), Nil))(at => bt => fTree(FinTree.rootValue(bt), List.reverse(Cons(FinTree.map(at)(h), FinTree.branches(bt)))))

  //  List.foldLeft[FinTree[A],FinTree[B]](FinTree.branches(t),fTree(h(FinTree.rootValue(t)),Nil))(at=>bt=>fTree(h(FinTree.rootValue(at)),Cons(FinTree.map(at)(h),FinTree.branches(bt))))
  def stringLength(strings: FinTree[String]): FinTree[Int] = map[String, Int](strings)(s => s.length)
}


object nith_Chapter_03 {

  import List.{Nil, Cons}

  //exercise 3.1
  def matchFun(l: List[Int]): Int = l match {
    case Cons(x, Cons(2, Cons(4, _))) => x
    //    case Nil => 42
    case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
    case Cons(h, t) => h + List.product(t)
    case _ => 101
  }

  def main(args: Array[String]) {

    val nilList: List[Double] = Nil
    val oneList: List[Int] = Cons(1, Nil)
    val strList: List[String] = Cons("a", Cons("b", Nil))
    val dblList: List[Double] = List(0.1, 1.2, 2.3, 3.4, 4.5)
    val fivList: List[Int] = List.integers(0)(4)
    val tenList: List[Int] = List.integers(0)(9)
    val hunList: List[Int] = List.integers(0)(99)
    val hunListReversed: List[Int] = List.integers(99)(0)
    val listOfLists: List[List[Int]] = List(List.Nil, oneList, fivList, tenList)

    log("****** Chapter_03 ******")
    log("************************")

    util.log("Int.MinValue = %s".format(Int.MinValue))
    util.log("Int.MaxValue = %s".format(Int.MaxValue))

    log("****** a few lists ******")
    log("nilList=" + List.myString(nilList))
    log("oneList=" + List.myString(oneList))
    log("strList=" + List.myString(strList))
    log("dblList=" + List.myString(dblList))
    log("fivList=" + List.myString(fivList))
    log("tenList=" + List.myString(tenList))
    log("hunList=" + List.myString(hunList))
    log("listOfLists=" + List.myString(listOfLists))
    log("****** playing with fivList ******")
    log("matchFun(fivList)=" + matchFun(fivList))
    log("tail(fivList)=" + List.tail(fivList))
    log("setHead(fivList,42)=" + List.setHead(fivList, 42))
    log("drop(fivList,-1)=" + List.drop(fivList, -1))
    log("drop(fivList,0)=" + List.drop(fivList, 0))
    log("drop(fivList,3)=" + List.drop(fivList, 3))
    log("drop(fivList,9)=" + List.drop(fivList, 9))
    log("dropWhile(fivList)(i=>(i==i))=" + List.dropWhile(fivList)(i => i == i))
    log("dropWhile(fivList)(i=>(i<4))=" + List.dropWhile(fivList)(i => i < 4))
    log("dropWhile(fivList)(i=>(i%3<2))=" + List.dropWhile(fivList)(i => i % 3 < 2))
    log("****** method init ******")
    log("init(oneList)=" + List.init(oneList))
    log("init(strList)=" + List.init(strList))
    log("init(fivList)=" + List.init(fivList))
    log("****** a few lists ******")
    log("nilList=" + List.myString(nilList))
    log("oneList=" + List.myString(oneList))
    log("strList=" + List.myString(strList))
    log("dblList=" + List.myString(dblList))
    log("fivList=" + List.myString(fivList))
    log("tenList=" + List.myString(tenList))
    log("hunList=" + List.myString(hunList))
    log("****** foldRight and foldLeft******")
    log("sum(fivList)=" + List.product(fivList))
    log("sum(tenList)=" + List.product(tenList))
    log("sum(hunList)=" + List.product(hunList))
    log("sumFoldRight(tenList)=" + List.sumFoldRight(tenList))
    log("sumFoldLeft(tenList)=" + List.sumFoldLeft(tenList))
    log("product(List.tail(fivList))=" + List.product(List.tail(fivList)))
    log("productFoldRight(tenList)=" + List.productFoldRight(tenList))
    log("productFoldLeft(tenList)=" + List.productFoldLeft(tenList))
    log("foldRight(fivList, Nil:List[Int])(a=>as=>Cons(a,as))))=" + List.foldRight(fivList, Nil: List[Int])(a => as => Cons(a, as)))
    log("length(fivList)=" + List.length(fivList))
    log("length(tenList)=" + List.length(tenList))
    log("length(hunList)=" + List.length(hunList))
    log("sumFoldRight(hunList)=" + List.sumFoldRight(hunList))
    log("sumFoldLeft(hunList)=" + List.sumFoldLeft(hunList))
    log("product(hunList)=" + List.product(hunList))
    log("productFoldRight(hunList)=" + List.productFoldRight(hunList))
    log("productFoldLeft(hunList)=" + List.productFoldLeft(hunList))
    log("reverse(tenList)=" + List.myString(List.reverse(tenList)))
    log("append(fivList,tenList)=" + List.myString(List.append(fivList, tenList)))
    log("concat(fivList,strList,tenList)=" + List.myString(List.concat(List(fivList, strList, tenList))))
    log("map(fivList)(n=>n+1)=" + List.myString(List.map(fivList)(n => n + 1)))
    log("map(dblList)(toString)=" + List.myString(List.map(dblList)(d => d.toString)))
    log("max(Nil)                                                 = " + List.max(List.Nil))
    log("max[Int](Nil)(i=>j=> i<j)(Int.MinValue)                  = " + List.max[Int](List.Nil)(i => j => i < j)(Int.MinValue))
    log("max(hunList)                                             = " + List.max(hunList))
    log("max[Int](hunList)(i=>j=>i<j)(Int.MinValue)               = " + List.max[Int](hunList)(i => j => i < j)(Int.MinValue))
    log("max[Int](List(0))(i=>j=>false)(Int.MinValue)             = " + List.max[Int](List(0))(i => j => false)(Int.MinValue))
    log("max[Int](hunList)(i=>j=>false)(Int.MinValue)             = " + List.max[Int](hunList)(i => j => false)(Int.MinValue))
    log("max[Int](hunListReversed)(i=>j=>false)(Int.MinValue)     = " + List.max[Int](hunListReversed)(i => j => false)(Int.MinValue))
    log("max[Int](List(0))(i=>j=>true)(Int.MinValue)              = " + List.max[Int](List(0))(i => j => true)(Int.MinValue))
    log("max[Int](hunList)(i=>j=>true)(Int.MinValue)              = " + List.max[Int](hunList)(i => j => true)(Int.MinValue))
    log("max[Int](hunListReversed)(i=>j=>true)(Int.MinValue)      = " + List.max[Int](hunListReversed)(i => j => true)(Int.MinValue))
    log("max[Int](List(0,1,2))(i=>j=>(i+j)%2==1)(Int.MinValue)    = " + List.max[Int](List(0, 1, 2))(i => j => (i + j) % 2 == 1)(Int.MinValue))
    log("max[Int](List(0,1,2,3))(i=>j=>(i+j)%2==1)(Int.MinValue)  = " + List.max[Int](List(0, 1, 2, 3))(i => j => (i + j) % 2 == 1)(Int.MinValue))
    log("flatMap(fivList)(n=>List(n+1))=" + List.myString(List.flatMap(fivList)(n => List(n + 1))))
    log("flatMap(fivList)(n=>List(n*n))=" + List.myString(List.flatMap(fivList)(n => List(n * n))))
    log("flatMap(fivList)(i => List(i,i)))=" + List.myString(List.flatMap(fivList)(i => List(i, i))))
    log("filter[Int](Nil)(n=>0==(n%2)=" + List.myString(List.filter[Int](Nil)(n => 0 == (n % 2))))
    log("filter(tenList)(n=>0==(n%2)=" + List.myString(List.filter(tenList)(n => 0 == (n % 2))))
    log("filter(tenList)(n=>0==(n%7)=" + List.myString(List.filter(tenList)(n => 0 == (n % 7))))
    log("flatFilter[Int](Nil)(n=>0==(n%2)=" + List.myString(List.flatFilter[Int](Nil)(n => 0 == (n % 2))))
    log("flatFilter(tenList)(n=>0==(n%2)=" + List.myString(List.flatFilter(tenList)(n => 0 == (n % 2))))
    log("flatFilter(tenList)(n=>0==(n%7)=" + List.myString(List.flatFilter(tenList)(n => 0 == (n % 7))))
    log("Pair(fivList)(tenList)(n=>m=>n+m)=" + List.myString(List.Pair(fivList)(tenList)))
    log("zipWith(fivList)(tenList)(n=>m=>n+m)=" + List.myString(List.zipWith(fivList)(tenList)(n => m => n + m)))
    log("zipWith(fivList)(tenList)(n=>m=>n*m)=" + List.myString(List.zipWith(fivList)(tenList)(n => m => n * m)))
    log("fivList=" + List.myString(fivList))
    log("tenList=" + List.myString(tenList))
    log("isInitialSegment(fivList,tenList,true)=" + List.isInitialSegment(fivList, tenList))
    log("isInitialSegment(tenList,fivList,true)=" + List.isInitialSegment(tenList, fivList))
    log("hasSubsequence(fivList,tenList)=" + List.hasSubsequence(fivList, tenList))
    log("hasSubsequence(tenList,fivList)=" + List.hasSubsequence(tenList, fivList))
    log("hasSubsequence(hunList,fivList)=" + List.hasSubsequence(hunList, fivList))
    log("hasSubsequence(hunList,tenList)=" + List.hasSubsequence(hunList, tenList))
    log("hasSubsequence(List(0,42,1,2,3,4),fivList)=" + List.hasSubsequence(List(0, 42, 1, 2, 3, 4), fivList))
    log("hasSubsequence(List(0,42,0,1,2,3,4),fivList)=" + List.hasSubsequence(List(0, 42, 0, 1, 2, 3, 4), fivList))
    log("****** Trees ******")
    log("size(Leaf(42))=" + Tree.size(Leaf(42)))
    log("size(Branch(Leaf(-1),Leaf(42)))=" + Tree.size(Branch(Leaf(-1), Leaf(42))))
    log("size(Branch(Branch(Leaf(-1),Leaf(0)),Leaf(42)))=" + Tree.size(Branch(Branch(Leaf(-1), Leaf(0)), Leaf(42))))
    log("sizeFold(Leaf(42))=" + Tree.sizeFold(Leaf(42)))
    log("sizeFold(Branch(Leaf(-1),Leaf(42)))=" + Tree.sizeFold(Branch(Leaf(-1), Leaf(42))))
    log("sizeFold(Branch(Branch(Leaf(-1),Leaf(0)),Leaf(42)))=" + Tree.sizeFold(Branch(Branch(Leaf(-100), Leaf(0)), Leaf(42))))
    log("maxVal(Leaf(42))=" + Tree.maxVal(Leaf(42)))
    log("maxVal(Branch(Leaf(-1),Leaf(42)))=" + Tree.maxVal(Branch(Leaf(-1), Leaf(42))))
    log("maxVal(Branch(Branch(Leaf(-1),Leaf(0)),Leaf(42)))=" + Tree.maxVal(Branch(Branch(Leaf(-100), Leaf(0)), Leaf(42))))
    log("depth(Leaf(42))=" + Tree.depth(Leaf(42)))
    log("depth(Branch(Leaf(-1),Leaf(42)))=" + Tree.depth(Branch(Leaf(-1), Leaf(42))))
    log("depth(Branch(Branch(Leaf(-1),Leaf(0)),Leaf(42)))=" + Tree.depth(Branch(Branch(Leaf(-100), Leaf(0)), Leaf(42))))
    log("stringLength(Leaf(a))=" + Tree.stringLength(Leaf("a")))
    log("stringLength(Branch(Leaf(a),Leaf(abc)))=" + Tree.stringLength(Branch(Leaf("a"), Leaf("abc"))))
    log("stringLength(Branch(Branch(Leaf(abc),Leaf()),Leaf(abcd)))="
      + Tree.stringLength(Branch(Branch(Leaf("abc"), Leaf("")), Leaf("abcd"))))
    log("****** Finitary branching trees with values at all nodes: FinTree ******")
    log("size(fTree(42,Nil))=" + FinTree.size(fTree(42, Nil)))
    log("size(fTree(123,List(fTree(-1,Nil),fTree(42,Nil)))))=" + FinTree.size(fTree(123, List(fTree(-1, Nil), fTree(42, Nil)))))
    log("size(fTree(456,List(fTree(123,List(fTree(-1,Nil),fTree(0,Nil))),fTree(42,Nil))))="
      + FinTree.size(fTree(456, List(fTree(123, List(fTree(-1, Nil), fTree(0, Nil))), fTree(42, Nil)))))
    log("maxVal(fTree(42,Nil))=" + FinTree.maxVal(fTree(42, Nil)))
    log("maxVal(fTree(123,List(fTree(-1,Nil),fTree(42,Nil)))))=" + FinTree.maxVal(fTree(123, List(fTree(-1, Nil), fTree(42, Nil)))))
    log("maxVal(fTree(456,List(fTree(123,List(fTree(-1,Nil),fTree(0,Nil))),fTree(42,Nil))))="
      + FinTree.maxVal(fTree(456, List(fTree(123, List(fTree(-1, Nil), fTree(0, Nil))), fTree(42, Nil)))))
    log("depth(fTree(42,Nil))=" + FinTree.depth(fTree(42, Nil)))
    log("depth(fTree(123,List(fTree(-1,Nil),fTree(42,Nil)))))=" + FinTree.depth(fTree(123, List(fTree(-1, Nil), fTree(42, Nil)))))
    log("depth(fTree(456,List(fTree(123,List(fTree(-1,Nil),fTree(0,Nil))),fTree(42,Nil))))="
      + FinTree.depth(fTree(456, List(fTree(123, List(fTree(-1, Nil), fTree(0, Nil))), fTree(42, Nil)))))
    log("depth(fTree(456,List(fTree(123,List(fTree(-1,List(fTree(7,Nil))),fTree(0,Nil))),fTree(42,List(fTree(7,Nil))))))="
      + FinTree.depth(fTree(456, List(fTree(123, List(fTree(-1, List(fTree(7, Nil))), fTree(0, Nil))), fTree(42, List(fTree(7, Nil)))))))
    log("****** string length for finTrees ******")
    log("stringLength(fTree(,Nil))=" + FinTree.stringLength(fTree("", Nil)))
    log("stringLength(fTree(abc,Nil))=" + FinTree.stringLength(fTree("abc", Nil)))
    log("stringLength(fTree(123,Nil))=" + FinTree.stringLength(fTree("123", Nil)))
    log("stringLength(fTree(123,List(fTree(,Nil))))=" + FinTree.stringLength(fTree("123", List(fTree("", Nil)))))
    log("stringLength(fTree(abc,List(fTree(abc,Nil))))=" + FinTree.stringLength(fTree("abc", List(fTree("abc", Nil)))))
    log("stringLength(fTree(123,List(fTree(ab,Nil)))))=" + FinTree.stringLength(fTree("123", List(fTree("ab", Nil)))))
    log("stringLength(fTree(123,List(fTree(a,Nil),fTree(ab,Nil)))))=" + FinTree.stringLength(fTree("123", List(fTree("a", Nil), fTree("ab", Nil)))))
    log("stringLength(fTree(456,List(fTree(123,List(fTree(ab,Nil),fTree(X,Nil))),fTree(abcdef,Nil))))="
      + FinTree.stringLength(fTree("456", List(fTree("123", List(fTree("ab", Nil), fTree("X", Nil))), fTree("abcdef", Nil)))))
    log("stringLength(fTree(456,List(fTree(123,List(fTree(ab,List(fTree(7,Nil))),fTree(X,Nil))),fTree(abcdef,List(fTree(7,Nil))))))="
      + FinTree.stringLength(fTree("456", List(fTree("123", List(fTree("ab", List(fTree("7", Nil))), fTree("X", Nil))), fTree("abcdef", List(fTree("7", Nil)))))))

    println("\n*** Additional staff ***")
    log("fill(-1)(\"*\")               = " + List.myString(List.fill(-1)("*")))
    log("fill(0)(\"*\")                = " + List.myString(List.fill(0)("*")))
    log("fill(1)(\"*\")                = " + List.myString(List.fill(1)("*")))
    log("fill(3)(\"*\")                = " + List.myString(List.fill(3)("*")))
    log("shovel(tenList)(fivList)(3) = " + List.myString(List.shovel(tenList)(fivList)(3)))
    log("halve(tenList)              = " + List.myString(List.halve(tenList)))
    log("halve(Nil)                  = " + List.myString(List.halve(Nil)))
    log("halve(List(\"a\"))            = " + List.myString(List.halve(List("a"))))
    log("integers(0)(0)              = " + List.myString(List.integers(0)(0)))
    log("integers(42)(0)             = " + List.myString(List.integers(42)(0)))
    log("integers(0)(42)             = " + List.myString(List.integers(0)(42)))

    println("\n*** Sorting ***")
    log("merge(List(0))(List(1))(n=>m=>n<m) = " + List.myString(List.merge[Int](List(0))(List(1))(n => m => n < m)))
    log("merge(fivList)(tenList)(n=>m=>n<m) = " + List.myString(List.merge[Int](fivList)(tenList)(n => m => n < m)))
    log("merge(fivList)(dblList)(n=>m=>n<m) = " + List.myString(List.merge[Double](List.map(fivList)(_.toDouble))(dblList)(n => m => n < m)))
    log("merge(dblList)(fivList)(n=>m=>n<m) = " + List.myString(List.merge[Double](dblList)(List.map(fivList)(_.toDouble))(n => m => n < m)))
    log("merge(hunList)(hunList)(n=>m=>n<m) = " + List.myString(List.merge[Int](hunList)(hunList)(n => m => n < m)))
    log("merge(listOfLists)(n=>m=>n<m)      = " + List.myString(List.merge[Int](listOfLists)(n => m => n < m)))
    log("splitIntoReversedListOfSingletons[Int](tenList) = " + List.myString(List.splitIntoReversedListOfSingletons[Int](tenList)))
    log("mergeSort[Int](tenList)(n=>m=>n<m) = " + List.myString(List.mergeSort[Int](hunList)(n => m => n < m)))
    log("mergeSort[Int](tenList)(n=>m=>n<m) = " + List.myString(List.mergeSort[Int](hunListReversed)(n => m => n < m)))
    log("isSorted[Int](hunList)                  = " + List.isSorted(hunList))
    log("isSorted[Int](hunListReversed)          = " + List.isSorted(hunListReversed))
    log("isSorted[Int](List(0,1,2,3,4,3,4,5,6))) = " + List.isSorted(List(0,1,2,3,4,3,4,5,6)))
  }
}
