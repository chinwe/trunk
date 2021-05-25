#pragma once

class MoveOnly {
public:
    // constructors:
    MoveOnly();
        
    // copying disabled:
    MoveOnly(const MoveOnly&) = delete;
    MoveOnly& operator= (const MoveOnly&) = delete;

    // moving enabled:
    MoveOnly(MoveOnly&&) noexcept;
    MoveOnly& operator= (MoveOnly&&) noexcept;
};
