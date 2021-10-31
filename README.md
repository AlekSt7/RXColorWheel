# RXColorWheel
RXColorWheel library - it is a fully customizable colorpicker element created based on the defauld android `View`.

## Usage

There is example of basic usage here:

XML:
```xml
    <com.example.rxcolorwheel.RXColorWheel
        android:id="@+id/YOUR_ID"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

Java:
```java

RXColorWheel colorPicker = findViewById(R.id.your_id_from_xml);

colorPicker.setColorChangeListener(new RXColorWheel.ColorChagneListener() {
            @Override
            public void onColorChanged(int color) {
               //Your code here
            }

            @Override
            public void firstDraw(int color) {
               //Your code here
            }
        });

```
