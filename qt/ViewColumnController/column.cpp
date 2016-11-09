#include "column.h"
#include "ui_column.h"

Column::Column(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Column)
{
    ui->setupUi(this);

    setWindowFlags(Qt::Popup);
}

Column::~Column()
{
    delete ui;
}

void Column::BindView(QTreeView *pView)
{
    if (nullptr == pView)
    {
        return;
    }

    m_pTreeView = pView;

    int iChkBoxCount = m_vecChkBox.size();
    for (int i = 0; i < iChkBoxCount; ++i)
    {
        ui->m_pVerticalLayout->removeWidget(m_vecChkBox.at(i));
        delete m_vecChkBox.at(i);
    }
    m_vecChkBox.clear();

    int iColCount = pView->model()->columnCount();

    for (int i = 0; i < iColCount; ++i)
    {
        QString strColTitle = pView->model()->headerData(i, Qt::Horizontal).toString();

        QCheckBox* pChkBox = new QCheckBox(strColTitle);
        pChkBox->setProperty("ColIndex", i);
        pChkBox->setChecked(m_pTreeView->isColumnHidden(i) ? Qt::Unchecked : Qt::Checked);

        connect(pChkBox, SIGNAL(stateChanged(int)), this, SLOT(OnStateChanged(int)));
        ui->m_pVerticalLayout->addWidget(pChkBox);

        m_vecChkBox.push_back(pChkBox);
    }

    setFixedSize(100, 24 * iColCount);
}

void Column::OnStateChanged(int iState)
{
    bool bHide = (Qt::Checked != iState);
    QCheckBox* pChkBox = qobject_cast<QCheckBox*>(sender());
    int iColIndex = pChkBox->property("ColIndex").toInt();

    m_pTreeView->setColumnHidden(iColIndex, bHide);
}
