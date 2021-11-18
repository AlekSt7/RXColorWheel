# RXColorWheel
RXColorWheel library - it is a fully customizable colorpicker element created based on the native android `View`.

<img src="https://user-images.githubusercontent.com/56515163/140645285-93adfb17-0554-48a6-8f2b-012c17573e96.png" height="350" width="528" />

## Download

Gradle:

    repositories {
            maven { url "https://jitpack.io" }
        }
    
    dependencies {
        implementation 'com.github.alekst7:rxcolorwheel:LATEST_RELEASE (for example v1.0.1)'
    }
    
## Usage

More details on the wiki of the project.

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

## License
MIT
