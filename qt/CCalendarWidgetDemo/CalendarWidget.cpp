#include "CalendarWidget.h"
#include <QPainter>

CCalendarWidget::CCalendarWidget(QWidget *parent)
    : QCalendarWidget(parent)
{
    setVerticalHeaderFormat(QCalendarWidget::NoVerticalHeader);
    setNavigationBarVisible(false);
    //setSelectionMode(QCalendarWidget::NoSelection);

    connect(this, SIGNAL(clicked(const QDate&)), this, SLOT(OnClick(const QDate&)));

    //去除选中背景色

     setStyleSheet("QAbstractItemView {selection-background-color: rgb(225, 0, 0, 0);"
                  "}");

    //setDateRange(QDate::currentDate().addMonths(-1).addDays(1), QDate::currentDate().addMonths(1).addDays(-1));
}

void CCalendarWidget::paintCell(QPainter *painter, const QRect &rect, const QDate &date) const
{
    if (nullptr == painter)
    {
        return;
    }

    if (m_vecHighlightDates.end() != qFind(m_vecHighlightDates, date))
    {
        // 高亮
        int iD = qMin(rect.width(), rect.height()) * 0.618;
        QRect rectDay(0, 0, iD, iD);
        rectDay.moveCenter(rect.center());

        painter->save();

        painter->setRenderHint(QPainter::Antialiasing, true);
        QPainterPath path;
        path.addEllipse(rectDay);
        painter->fillPath(path, Qt::red);
        painter->drawText(rect, Qt::TextSingleLine | Qt::AlignCenter,QString::number(date.day()));

        painter->restore();
    }
    else
    {
        // 正常
        //painter->drawText(rect, Qt::TextSingleLine | Qt::AlignCenter,QString::number(date.day()));
        QCalendarWidget::paintCell(painter, rect, date);
    }
}

void CCalendarWidget::OnClick(const QDate &date)
{
    auto it = qFind(m_vecHighlightDates.begin(), m_vecHighlightDates.end(), date);
    if (it == m_vecHighlightDates.end())
    {
        m_vecHighlightDates.push_back(date);
    }
    else
    {
        m_vecHighlightDates.erase(it);
    }

    updateCell(date);
}


