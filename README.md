# TradeVision Pro

TradeVision Pro is a modern, responsive, high-performance desktop trading application built with **JavaFX** and **Lightweight Charts**. It aims to replicate the core features and premium aesthetics of TradingView, designed specifically for a seamless user experience.

## 🚀 Features

* **Advanced Charting**: Powered by TradingView's Lightweight Charts, fully integrated into a JavaFX WebView.
* **Custom Drawing Engine**: A bespoke SVG-based drawing engine overlaid perfectly on the chart, enabling complex drawings even in empty future timeframes. Includes:
  * **Fibonacci Retracement** (Color-coded, custom coordinate extension)
  * **Custom Trend Lines** (Arbitrary point-to-point drawing)
  * **Rectangles / ICT Blocks** (FVG & Order Block area highlighting)
  * **Text Markers** (Click anywhere to stamp custom notes)
* **Live Market Simulation**: Real-time simulated price action that updates the candlesticks and Watchlist simultaneously.
* **Interactive Watchlist**: Real-time ticker tape with live updating prices and percentage changes.
* **Order Panel**: Built-in panel for Buy/Sell executions with quick lot size adjustments and portfolio balance tracking.
* **Dynamic Timeframes**: Switch instantly between 1m, 5m, 15m, 1H, and 1D charts, featuring accurate candle-close countdown timers.
* **Customizable Settings**: Built-in functional settings dialog to toggle grid lines, crosshair modes (Magnet/Normal), and price scales.
* **Theme Engine**: Instantly toggle between a beautifully crafted Dark Mode and Light Mode.

## 🛠️ Technology Stack

* **Java 21**
* **JavaFX 21** (UI Framework & WebEngine)
* **Maven** (Dependency Management)
* **HTML/CSS/JavaScript** (Chart rendering logic)
* **Lightweight Charts v4** (TradingView open-source charting library)

## 🏃‍♂️ How to Run

1. Ensure you have **Java 21** or higher and **Maven** installed on your system.
2. Open a terminal in the project root directory.
3. Simply run the provided batch file:
   ```cmd
   Run-TradeVision.bat
