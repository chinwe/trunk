#include "RingBuffer.h"

#include <iostream>
#include <thread>
#include <functional>
#include <cassert>

constexpr size_t kReadSize = 1000;
constexpr size_t kWriteSize = 2000;

std::atomic_bool g_exit{ false };
RingBuffer g_ring_buffer(100, 4000);
std::atomic<size_t> g_total_read_size{ 0 };
std::atomic<size_t> g_total_write_size{ 0 };

void ReadThread()
{
    std::string buffer(kReadSize, 'r');
    std::srand(time(nullptr));

    while (!g_exit.load())
    {
        size_t read_size = 0;
        do
        {
            read_size = g_ring_buffer.Read(const_cast<char*>(buffer.data()), std::rand() % kReadSize + 1);
            // std::cout << "read_size=" << read_size << std::endl;
            g_total_read_size += read_size;

            //std::this_thread::sleep_for(std::chrono::milliseconds(500));
        } while (0 == read_size);
    }
    
}

void WriteThread()
{
    std::string buffer(kWriteSize, 'w');

    while (!g_exit.load())
    {
        size_t write_size = 0;
        do
        {
            write_size = g_ring_buffer.Write(buffer.data(), std::rand() % kWriteSize + 1);
            // std::cout << "write_size=" << write_size << std::endl;
            g_total_write_size += write_size;

            //std::this_thread::sleep_for(std::chrono::milliseconds(500));
        } while (0 == write_size);
    }
}

int main(int argc, char* argv[])
{
    std::string read_data;
    std::string write_data;

    do
    {
        g_exit.store(false);

        std::thread th_read(ReadThread);
        std::thread th_write(WriteThread);

        std::this_thread::sleep_for(std::chrono::seconds(10));

        g_exit.store(true);
        th_read.join();
        th_write.join();

        assert(g_total_write_size == (g_ring_buffer.BufferSize() + g_total_read_size));
        std::cout << "--------------------------------------------------------------" << std::endl;

        std::cout << "total_write_size=" << g_total_write_size
            << ",total_read_size=" << g_total_read_size
            << ",BufferSize=" << g_ring_buffer.BufferSize()
            << std::endl;

    } while (true);

    return 0;
}