package util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


// (a,b,c), 取值范围,[a,b),
public class MyUtils {
	
//    public static void main(String[] args) {
//    	for(int i=0;i<5;i++) {
//            Set<Integer> set = getRandoms(0, 10, 5);
//            System.out.println("数量：" + set.size());
//            for (Integer s : set) {
//                System.out.println(s);
//            }
//    	}
//
//    }

    /**
     * 生成一组不重复随机数 (a,b,c)  范围是 [a,b)
     *
     * @param start 开始位置：可以为负数
     * @param end   结束位置：end > start
     * @param count 数量 >= 0
     * @return
     */
    public static Set<Integer> getRandoms(int start, int end, int count) {
        // 参数有效性检查
        if (start > end || count < 1) {
            count = 0;
        }
        // 结束值 与 开始值 的差小于 总数量
        if ((end - start) < count) {
            count = (end - start) > 0 ? (end - start) : 0;
        }

        // 定义存放集合
        Set<Integer> set = new HashSet<>(count);
        if (count > 0) {
            Random r = new Random();
            // 一直生成足够数量后再停止
            while (set.size() < count) {
                set.add(start + r.nextInt(end - start));
            }
        }
        return set;
    }
    
    
    //排序并返回序号，这是从小到大的排序
    public static int[] sortIndex(double a[]) {
		int count = 0;//用于加入到数组中
		int oriSortIndex[] = new int[a.length];
		Number sorted[] = new Number[a.length];
        for (int i = 0; i < a.length; ++i) {
            sorted[i] = new Number(a[i], i);
        }
        Arrays.sort(sorted);
      //print sorted array
//        for (Number n : sorted){
//            System.out.print("" + n.data +",");
//        }
//        System.out.println();

        // print original index
    
        for (Number n: sorted){
//            System.out.print("" + n.index + ",");
            oriSortIndex[count++] = n.index;
        }
        return oriSortIndex;
        
	}
    
    
}


//辅助，排序并返回原来序号的方法
class Number implements Comparable<Number>{
  Double data;
  int index;

  Number(double d, int i){
      this.data = d;
      this.index = i;
  }
  
  @Override
  public int compareTo(Number o) {
      return this.data.compareTo(o.data);
  }
}
