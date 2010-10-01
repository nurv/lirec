/****************************************************************************
** Meta object code from reading C++ file 'server3Gui.h'
**
** Created: Fri 10. Sep 14:35:02 2010
**      by: The Qt Meta Object Compiler version 62 (Qt 4.6.3)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "server3Gui.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'server3Gui.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 62
#error "This file was generated using the moc from 4.6.3. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_LogReadoutWindow[] = {

 // content:
       4,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      18,   17,   17,   17, 0x08,
      30,   17,   17,   17, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_LogReadoutWindow[] = {
    "LogReadoutWindow\0\0ClearText()\0SaveTxt()\0"
};

const QMetaObject LogReadoutWindow::staticMetaObject = {
    { &QWidget::staticMetaObject, qt_meta_stringdata_LogReadoutWindow,
      qt_meta_data_LogReadoutWindow, 0 }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &LogReadoutWindow::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *LogReadoutWindow::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *LogReadoutWindow::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_LogReadoutWindow))
        return static_cast<void*>(const_cast< LogReadoutWindow*>(this));
    return QWidget::qt_metacast(_clname);
}

int LogReadoutWindow::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QWidget::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: ClearText(); break;
        case 1: SaveTxt(); break;
        default: ;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_ToolWindow[] = {

 // content:
       4,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      14,   12,   11,   11, 0x08,
      37,   11,   11,   11, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_ToolWindow[] = {
    "ToolWindow\0\0c\0connectionchanged(int)\0"
    "Thanks()\0"
};

const QMetaObject ToolWindow::staticMetaObject = {
    { &QWidget::staticMetaObject, qt_meta_stringdata_ToolWindow,
      qt_meta_data_ToolWindow, 0 }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &ToolWindow::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *ToolWindow::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *ToolWindow::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_ToolWindow))
        return static_cast<void*>(const_cast< ToolWindow*>(this));
    return QWidget::qt_metacast(_clname);
}

int ToolWindow::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QWidget::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: connectionchanged((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 1: Thanks(); break;
        default: ;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_MyConnectionView[] = {

 // content:
       4,       // revision
       0,       // classname
       0,    0, // classinfo
       7,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      18,   17,   17,   17, 0x08,
      29,   17,   17,   17, 0x08,
      40,   17,   17,   17, 0x08,
      57,   53,   17,   17, 0x08,
      67,   17,   17,   17, 0x08,
      77,   17,   17,   17, 0x08,
      90,   17,   17,   17, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_MyConnectionView[] = {
    "MyConnectionView\0\0SaveAllM()\0LoadAllM()\0"
    "updateline()\0num\0zoom(int)\0zoomOut()\0"
    "rotateLeft()\0rotateRight()\0"
};

const QMetaObject MyConnectionView::staticMetaObject = {
    { &QGraphicsView::staticMetaObject, qt_meta_stringdata_MyConnectionView,
      qt_meta_data_MyConnectionView, 0 }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &MyConnectionView::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *MyConnectionView::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *MyConnectionView::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_MyConnectionView))
        return static_cast<void*>(const_cast< MyConnectionView*>(this));
    return QGraphicsView::qt_metacast(_clname);
}

int MyConnectionView::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QGraphicsView::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: SaveAllM(); break;
        case 1: LoadAllM(); break;
        case 2: updateline(); break;
        case 3: zoom((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 4: zoomOut(); break;
        case 5: rotateLeft(); break;
        case 6: rotateRight(); break;
        default: ;
        }
        _id -= 7;
    }
    return _id;
}
QT_END_MOC_NAMESPACE
