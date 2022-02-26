# MaterialYouFileExplorer
A file explorer with the style of Material You.

Use this library to select files/directories quickly.

## Implementation
1. Enable `mavenCentral()` in `settings.gradle`
```
repositories {
        ......
        mavenCentral()
    }
```
2. Implementation
```
implementation 'io.github.xayahsususu:materialyoufileexplorer:1.0.0'
```

## Usage
1. Initialize in `onCreate()`
```
val materialYouFileExplorer = MaterialYouFileExplorer()
materialYouFileExplorer.initialize(this) {
    // Code here
    // Get path string from `it`
}
```
2. Start the explorer activity
```
materialYouFileExplorer.toExplorer(this, $isFile)
```
3. Sample
```
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val materialYouFileExplorer = MaterialYouFileExplorer()
        materialYouFileExplorer.initialize(this) {
            binding.textInputEditText.setText(it)
        }

        binding.filledButton.setOnClickListener {
            materialYouFileExplorer.toExplorer(this, binding.radioButtonFile.isChecked)
        }
    }
}
```