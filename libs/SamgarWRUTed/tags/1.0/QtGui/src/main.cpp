#include <QtGui/QApplication>
#include "samgargui.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    SamgarGui w;
    w.show();
    return a.exec();
}
