#ifndef CCALENDARWIDGET_H
#define CCALENDARWIDGET_H

#include <QCalendarWidget>

class CCalendarWidget : public QCalendarWidget
{
    Q_OBJECT
public:
    explicit CCalendarWidget(QWidget *parent = 0);

protected:
    virtual void paintCell(QPainter *painter, const QRect &rect, const QDate &date) const;

private slots:
    void OnClick(const QDate &date);

private:
    QVector<QDate> m_vecHighlightDates;
};

#endif // CCALENDARWIDGET_H
