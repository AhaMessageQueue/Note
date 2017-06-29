```text
SETNX key value
```
将 key 的值设为 value ，当且仅当 key 不存在。

若给定的 key 已经存在，则 SETNX 不做任何动作。

SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。

**可用版本：**

    >= 1.0.0
    
**时间复杂度：**

    O(1)
    
**返回值：**

    设置成功，返回 1 。
    设置失败，返回 0 。

```text
redis> EXISTS job                # job 不存在
(integer) 0

redis> SETNX job "programmer"    # job 设置成功
(integer) 1

redis> SETNX job "code-farmer"   # 尝试覆盖 job ，失败
(integer) 0

redis> GET job                   # 没有被覆盖
"programmer"
```







