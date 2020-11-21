#pragma once

#include <atomic>
#include <string>

class RingBuffer
{
public:
    RingBuffer(int init_capacity, int max_capacity);
    ~RingBuffer();

    // 缓冲区容量
    int BufferCapacity() const;

    // 是否可读
    bool ReadAvailable() const;

    // 是否可写
    bool WriteAvailable(size_t write_len) const;

    // 实际数据大小
    int BufferSize() const;

    // 读取
    size_t Read(char* ptr_out, size_t out_size);

    // 写入
    size_t Write(const char* ptr_data, size_t data_size);

    // 查找
    // int Find(const std::string& token);

private:
    // 扩容
    bool CapacityExpansion();

private:
    std::atomic<size_t> capacity_{ 0 };
    std::atomic<size_t> max_capacity_{ 0 };
    char* ptr_buffer_{ nullptr };
    std::atomic<size_t> write_{ 0 };
    std::atomic<size_t> read_{ 0 };
    std::atomic<size_t> buffer_size{ 0 };
};
