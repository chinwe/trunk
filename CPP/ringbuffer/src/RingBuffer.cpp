#include "RingBuffer.h"
#include <utility>

RingBuffer::RingBuffer(int init_capacity, int max_capacity)
    : capacity_(init_capacity),
    max_capacity_(max_capacity),
    ptr_buffer_(new char[capacity_]{ 0 })
{

}

RingBuffer::~RingBuffer()
{
    delete[] ptr_buffer_;
    ptr_buffer_ = nullptr;
}

// 缓冲区容量
size_t RingBuffer::BufferCapacity() const
{
    return capacity_.load();
}

// 是否可读
bool RingBuffer::ReadAvailable() const
{
    return (buffer_size.load() > 0);
}

// 是否可写
bool RingBuffer::WriteAvailable(size_t write_len)
{
    if ((buffer_size.load() + write_len) <= capacity_.load())
    {
        return true;
    }
    
    // 容量不足，扩容
    while ((buffer_size.load() + write_len) > capacity_.load() && (capacity_.load() < max_capacity_.load()))
    {
        CapacityExpansion(write_len);
    }

    return (buffer_size.load() + write_len) > capacity_.load();
}

// 实际数据大小
size_t RingBuffer::BufferSize() const
{
    return buffer_size.load();
}

// 读取
size_t RingBuffer::Read(char* ptr_out, size_t out_size)
{
    if (!ReadAvailable())
    {
        return 0;
    }
    
    size_t read_size = std::min(out_size, buffer_size.load());
    if ((read_ + read_size)  < capacity_)
    {
        memcpy(ptr_out, ptr_buffer_ + read_.load(), read_size);
    }
    else
    {
        // 超出尾部，需要读两次
        size_t tail_size = capacity_ - read_.load();
        memcpy(ptr_out, ptr_buffer_ + read_.load(), tail_size);
        memcpy(ptr_out + tail_size, ptr_buffer_, read_size - tail_size);
    }
    
    read_ = (read_ + read_size) % capacity_;
    buffer_size.fetch_sub(read_size);

    return read_size;
}

// 写入
size_t RingBuffer::Write(const char* ptr_data, size_t data_size)
{
    if (!WriteAvailable(data_size))
    {
        return 0;
    }
    
    size_t write_size = std::min(data_size, capacity_.load() - buffer_size.load());
    if ((write_ + write_size)  < capacity_)
    {
        memcpy(ptr_buffer_ + write_.load(), ptr_data, write_size);
    }
    else
    {
        // 超出尾部，需要读两次
        size_t tail_size = capacity_ - write_.load();
        memcpy(ptr_buffer_ + write_.load(), ptr_data, tail_size);
        memcpy(ptr_buffer_, ptr_data + tail_size, write_size - tail_size);
    }
    
    write_ = (write_ + write_size) % capacity_;
    buffer_size.fetch_add(write_size);

    return write_size;
}

// 查找
// int RingBuffer::Find(const std::string& token)
// {
//     return 0;
// }

// 扩容
bool RingBuffer::CapacityExpansion(size_t write_len)
{
    if (capacity_.load() >= max_capacity_.load())
    {
        // 已是最大容量
        return false;
    }

    size_t need_size = (buffer_size.load() + write_len);
    size_t expansion_size = 0;
    do
    {
        expansion_size += capacity_.load();
    } while ((capacity_ + expansion_size) < need_size);

    if ((capacity_.load() + expansion_size) > max_capacity_.load())
    {
        // 可扩展空间不足
        expansion_size = (max_capacity_.load() - capacity_.load());
    }

    // 重新分配缓冲区
    char* ptr_new_buffer = new char[capacity_.load() + expansion_size]{ 0 };
    memcpy(ptr_new_buffer, ptr_buffer_, capacity_.load());

    if (write_.load() < read_.load())
    {
        // 需要把原缓冲区的数据拷贝到扩容区域
        memcpy(ptr_new_buffer + capacity_.load(), ptr_buffer_, write_.load());
        write_.fetch_add(capacity_.load());
    }
  
    delete[ ] ptr_buffer_;
    ptr_buffer_ = ptr_new_buffer;

    // 调整大小
    capacity_.fetch_add(expansion_size);

    return false;
}
