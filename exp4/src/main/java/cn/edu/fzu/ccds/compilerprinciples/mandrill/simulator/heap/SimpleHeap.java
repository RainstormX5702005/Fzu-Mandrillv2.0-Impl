package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.heap;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的堆分配器，以 4 字节（一个 long 的低 32 位）为单位分配和读写。
 * 地址以字节为单位，且始终 4 字节对齐。
 */
public class SimpleHeap {
    private static final long HEAP_BASE = 0x10000L;
    private final List<Long> memory = new ArrayList<>();

    /**
     * 分配指定字节数的内存，返回字节地址。
     */
    public long allocate(int bytes) {
        if (bytes <= 0) {
            bytes = 4;
        }
        int words = (bytes + 3) / 4;
        long address = HEAP_BASE + (long) memory.size() * 4L;
        for (int i = 0; i < words; i++) {
            memory.add(0L);
        }
        return address;
    }

    /**
     * 从指定字节地址读取一个 4 字节字。
     */
    public long read(long byteAddress) {
        int index = (int) ((byteAddress - HEAP_BASE) / 4);
        if (index < 0 || index >= memory.size()) {
            return 0L;
        }
        return memory.get(index);
    }

    /**
     * 向指定字节地址写入一个 4 字节字。
     */
    public void write(long byteAddress, long value) {
        int index = (int) ((byteAddress - HEAP_BASE) / 4);
        if (index < 0 || index >= memory.size()) {
            return;
        }
        memory.set(index, value & 0xFFFFFFFFL);
    }
}
