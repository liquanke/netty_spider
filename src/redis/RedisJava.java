package redis;

import redis.clients.jedis.Jedis;

public class RedisJava {
	public static void main(String[] args) {
		Jedis jedis = new Jedis("localhost",6379);
		//如果redis有密码的jedis.auth(redis的密码);需要此语句链接
		System.out.println("connect successful ! "+jedis.ping());

		jedis.append("first","hello");//找到此key对应的值，在value字符串后追加字符串 如果此key不存在将创建
		String value1 = jedis.get("first");//获取指定key下的values的值
		System.out.println(value1);
		String value2 = jedis.get("second");
		System.out.println(value2);

		//jedis.del("first");//删除指定的key

		jedis.flushAll();//删除所有的key
	}
	public void post (String key,String value) {
		if(value != null) {
		Jedis jedis = new Jedis("localhost",6379);
		jedis.append(key, value);
		System.out.println("post successful !");
		}
	}
	public String get (String key) {
		Jedis jedis = new Jedis("localhost",6379);
		String value = jedis.get(key);
		System.out.println("get successful !");
		return value ;
	}
	public void del (String key) {
		Jedis jedis = new Jedis("localhost",6379);
		jedis.del(key);
		System.out.println("del successful !");
	}
	public void delall () {
		Jedis jedis = new Jedis("localhost",6379);
		jedis.flushAll();
		System.out.println("delall successful !");
	}

}
