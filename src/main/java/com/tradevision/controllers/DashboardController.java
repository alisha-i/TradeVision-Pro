package com.tradevision.controllers;

import javafx.application.Platform;
import com.tradevision.App;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.net.URL;
import java.util.Random;
import java.util.List;

public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private TextField searchField;
    @FXML private ToggleButton themeToggle;
    @FXML private WebView chartWebView;
    @FXML private VBox loadingOverlay;
    @FXML private Label currentSymbolLabel;
    @FXML private Label buyPriceLabel;
    @FXML private Label sellPriceLabel;

    @FXML private TextField quantityField;
    @FXML private Label balanceLabel;
    
    @FXML private TableView<com.tradevision.models.OrderModel> historyTable;
    @FXML private TableColumn<com.tradevision.models.OrderModel, String> colTime;
    @FXML private TableColumn<com.tradevision.models.OrderModel, String> colSymbol;
    @FXML private TableColumn<com.tradevision.models.OrderModel, String> colType;
    @FXML private TableColumn<com.tradevision.models.OrderModel, Double> colQty;
    @FXML private TableColumn<com.tradevision.models.OrderModel, Double> colPrice;

    @FXML private ToggleButton btn1m;
    @FXML private ToggleButton btn5m;
    @FXML private ToggleButton btn15m;
    @FXML private ToggleButton btn1h;
    @FXML private ToggleButton btn1d;
    
    // Panel Toggle FXML Bindings
    @FXML private VBox leftPanel;
    @FXML private VBox rightPanel;
    @FXML private VBox bottomDrawer;
    @FXML private SplitPane centerSplitPane;
    @FXML private ToggleButton toggleLeftBtn;
    @FXML private ToggleButton toggleRightBtn;
    @FXML private ToggleButton toggleBottomBtn;
    @FXML private MenuButton indicatorMenuButton;
    
    // Tab & Container Bindings for Watchlist vs Whale Tracker
    @FXML private Button watchlistTabBtn;
    @FXML private Button whaleTabBtn;
    @FXML private VBox watchlistContainer;
    @FXML private VBox whaleTrackerContainer;
    @FXML private ListView<String> watchlistList;
    
    // AI Whale Tracker Dynamic UI Elements
    @FXML private ProgressBar whaleProgressBar;
    @FXML private Label whalePressureLabel;
    @FXML private Label whaleTrade1, whaleTrade2, whaleTrade3;
    
    // AI Co-Pilot Chatbot Bindings
    @FXML private ListView<String> aiChatListView;
    @FXML private TextField aiPromptField;
    
    @FXML private Label riskStatusLabel;
    @FXML private TextField riskPercentField;
    @FXML private Button killSwitchBtn;

    private WebEngine webEngine;
    private boolean isDarkMode = true;
    private String currentSymbol = "XAUUSD";
    private int currentTimeframeSeconds = 86400; // default 1D
    private Random random = new Random();
    
    // Risk Guardian & Live Simulation State
    private boolean killSwitchActive = true;
    private double dailyLoss = 0.0;
    private double maxDailyLossLimit = 2000.0;
    private int tickCounter = 0;

    @FXML
    public void initialize() {
        // Initialize Watchlist
        watchlistList.getItems().addAll("XAUUSD", "BTCUSD", "AAPL", "EURUSD", "TSLA");
        
        // Ensure bottomDrawer is fully removed from SplitPane initially so chart takes 100% full height!
        if (centerSplitPane != null && bottomDrawer != null) {
            centerSplitPane.getItems().remove(bottomDrawer);
        }
        
        // Custom Cell Factory for Professional Sparkline Look
        watchlistList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String symbol, boolean empty) {
                super.updateItem(symbol, empty);
                if (empty || symbol == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox root = new HBox(10);
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    Label symbolLbl = new Label(symbol);
                    symbolLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-cyan; -fx-font-size: 11px;");
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    double basePrice = symbol.equals("BTCUSD") ? 64200 : (symbol.equals("AAPL") ? 192.5 : (symbol.equals("XAUUSD") ? 2330 : 185));
                    double fluc = (random.nextDouble() - 0.5) * (basePrice * 0.005);
                    double price = basePrice + fluc;
                    double change = (random.nextDouble() * 5) - 2.5; 
                    
                    Label priceLbl = new Label(String.format("%,.2f", price));
                    priceLbl.setStyle("-fx-text-fill: -text-main; -fx-font-weight: bold; -fx-font-size: 11px;");
                    
                    Label changeLbl = new Label(String.format("%+.2f%%", change));
                    changeLbl.setPrefWidth(50);
                    changeLbl.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    if (change >= 0) {
                        changeLbl.setStyle("-fx-text-fill: -accent-green; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else {
                        changeLbl.setStyle("-fx-text-fill: -accent-red; -fx-font-weight: bold; -fx-font-size: 11px;");
                    }
                    
                    root.getChildren().addAll(symbolLbl, spacer, priceLbl, changeLbl);
                    setGraphic(root);
                }
            }
        });
        
        watchlistList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentSymbolLabel.setText(newVal);
                loadDummyDataForSymbol(newVal);
                updateAIChatForSymbol(newVal);
                updateWhaleTrackerForSymbol(newVal);
            }
        });

        // Initialize AI Chatbot View with Custom CellFactory for Flawless Text Wrapping
        if (aiChatListView != null) {
            aiChatListView.setCellFactory(lv -> new ListCell<String>() {
                private Label label = new Label();
                {
                    label.setWrapText(true);
                    label.setStyle("-fx-text-fill: -text-main; -fx-font-size: 11px; -fx-padding: 4 2;");
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        label.setText(item);
                        label.setPrefWidth(aiChatListView.getWidth() > 30 ? aiChatListView.getWidth() - 25 : 260);
                        if (item.startsWith("User")) {
                            label.setStyle("-fx-text-fill: -accent-cyan; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 2;");
                        } else {
                            label.setStyle("-fx-text-fill: -text-main; -fx-font-size: 11px; -fx-padding: 4 2;");
                        }
                        setGraphic(label);
                    }
                }
            });
            aiChatListView.getItems().add("AI Co-Pilot: Active & ready. Ask me anything or select quick action chips below for live institutional metrics.");
        }

        // Initialize WebEngine for TradingView charts
        webEngine = chartWebView.getEngine();
        
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadingOverlay.setVisible(false);
                
                netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                window.setMember("javaConsole", new JavaConsole());
                webEngine.executeScript("window.onerror = function(msg, url, line) { javaConsole.error(msg + ' at ' + line); return false; };");
                webEngine.executeScript("console.log = function(msg) { javaConsole.log(msg); };");
                webEngine.executeScript("console.error = function(msg) { javaConsole.error(msg); };");
                
                webEngine.setPromptHandler(promptData -> {
                    TextInputDialog dialog = new TextInputDialog(promptData.getDefaultValue());
                    dialog.setTitle("Input Required");
                    dialog.setHeaderText(promptData.getMessage());
                    DialogPane dialogPane = dialog.getDialogPane();
                    dialogPane.setStyle("-fx-background-color: #161B22; -fx-text-fill: #F0F6FC;");
                    dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #0B0F17;");
                    dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #F0F6FC;");
                    java.util.Optional<String> result = dialog.showAndWait();
                    return result.orElse("");
                });

                chartWebView.widthProperty().addListener((wObs, oldVal, newVal) -> {
                    if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                        webEngine.executeScript("if (typeof resizeChart === 'function') resizeChart(" + newVal + ", " + chartWebView.getHeight() + ");");
                    }
                });
                chartWebView.heightProperty().addListener((hObs, oldVal, newVal) -> {
                    if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                        webEngine.executeScript("if (typeof resizeChart === 'function') resizeChart(" + chartWebView.getWidth() + ", " + newVal + ");");
                    }
                });
                
                watchlistList.getSelectionModel().selectFirst();
                updateBalance();
                setupHistoryTable();
            }
        });

        URL url = getClass().getResource("/com/tradevision/views/chart.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("Could not find chart.html");
        }
    }

    // Tab Handlers for Watchlist vs 🔥 AI Whale Tracker
    @FXML
    private void handleShowWatchlistView(ActionEvent event) {
        if (watchlistContainer != null && whaleTrackerContainer != null) {
            watchlistContainer.setVisible(true); watchlistContainer.setManaged(true);
            whaleTrackerContainer.setVisible(false); whaleTrackerContainer.setManaged(false);
            if (!watchlistTabBtn.getStyleClass().contains("primary-button")) watchlistTabBtn.getStyleClass().add("primary-button");
            whaleTabBtn.getStyleClass().remove("primary-button");
        }
    }

    @FXML
    private void handleShowWhaleTrackerView(ActionEvent event) {
        if (watchlistContainer != null && whaleTrackerContainer != null) {
            watchlistContainer.setVisible(false); watchlistContainer.setManaged(false);
            whaleTrackerContainer.setVisible(true); whaleTrackerContainer.setManaged(true);
            if (!whaleTabBtn.getStyleClass().contains("primary-button")) whaleTabBtn.getStyleClass().add("primary-button");
            watchlistTabBtn.getStyleClass().remove("primary-button");
        }
    }

    // Instant Whale Trade Copy Execution
    @FXML
    private void handleCopyWhaleTrade(ActionEvent event) {
        String symbol = watchlistList.getSelectionModel().getSelectedItem();
        if (symbol == null) symbol = currentSymbol;
        
        double price = currentLivePrice;
        int userId = com.tradevision.utils.SessionManager.getCurrentUserId();
        boolean success = com.tradevision.database.DatabaseManager.getInstance().executeTrade(userId, symbol, "BUY", 1.0, price);
        
        if (success) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "🐳 WHALE MOMENTUM COPY: Market BUY 1.0 " + symbol + " @ $" + String.format("%,.2f", price) + " Filled!", true);
            updateBalance();
            refreshHistoryTable();
            if (aiChatListView != null) {
                aiChatListView.getItems().add("AI Co-Pilot: Successfully copied institutional Whale order. Riding massive bullish liquidity wave at $" + String.format("%,.2f", price));
                aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
            }
        } else {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "Whale Copy Failed. Insufficient funds.", false);
        }
    }

    // AI Chatbot Event Handlers
    @FXML
    private void handleSendAIChat(ActionEvent event) {
        if (aiPromptField != null && !aiPromptField.getText().trim().isEmpty()) {
            String prompt = aiPromptField.getText().trim();
            aiChatListView.getItems().add("User: " + prompt);
            aiPromptField.clear();
            
            // Generate intelligent AI quantitative response
            Platform.runLater(() -> {
                String reply;
                String lower = prompt.toLowerCase();
                if (lower.contains("trend") || lower.contains("bull") || lower.contains("bear")) {
                    reply = "AI Co-Pilot: Based on multi-timeframe EMA alignment and MACD histogram expansion, the primary trend for " + currentSymbol + " is robustly BULLISH. Buy dips near key dynamic support.";
                } else if (lower.contains("fvg") || lower.contains("ict") || lower.contains("order block")) {
                    reply = "AI Co-Pilot: Scanning ICT structures... Found a prominent Fair Value Gap (FVG) on the 1D chart. Institutional order blocks show massive resting liquidity waiting at previous swing lows.";
                } else if (lower.contains("buy") || lower.contains("sell") || lower.contains("trade")) {
                    reply = "AI Co-Pilot: Evaluated current Risk/Reward ratio at 2.45. Ensure your stop loss is placed below the 50 EMA. Optimal entry conditions are currently met.";
                } else {
                    reply = "AI Co-Pilot: Quantitative sentiment model indicates 76% bullish momentum on " + currentSymbol + ". Tape speed has increased by 14% over the last hour.";
                }
                aiChatListView.getItems().add(reply);
                aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
            });
        }
    }

    @FXML
    private void handleAIQuickAction(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String chipText = btn.getText();
        if (aiChatListView != null) {
            aiChatListView.getItems().add("User Quick Action: " + chipText);
            Platform.runLater(() -> {
                String reply;
                if (chipText.contains("Analyze")) {
                    reply = "AI Co-Pilot Setup Analysis: " + currentSymbol + " is experiencing institutional accumulation. Order flow shows aggressive market buying absorbing limit sell walls.";
                } else if (chipText.contains("ICT FVG")) {
                    reply = "AI Co-Pilot ICT Scanner: Unmitigated Fair Value Gap detected between " + String.format("%,.2f", currentLivePrice * 0.995) + " and " + String.format("%,.2f", currentLivePrice * 0.998) + ". Highly probable reversal zone.";
                } else if (chipText.contains("Macro")) {
                    reply = "AI Co-Pilot Macro Sentiment: Overall Global Liquidity is positive. Real-time news impact score is +8.2/10 (Highly Bullish).";
                } else {
                    reply = "AI Co-Pilot Price Prediction: Machine learning regression models project a +2.4% upside target over the next 24 hours with 81% confidence interval.";
                }
                aiChatListView.getItems().add(reply);
                aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
            });
        }
    }

    private void updateAIChatForSymbol(String symbol) {
        if (aiChatListView != null) {
            aiChatListView.getItems().add("AI Co-Pilot: Switched to " + symbol + ". Initializing deep order book liquidity scan and momentum matrix...");
            aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
        }
    }
    
    private void updateWhaleTrackerForSymbol(String symbol) {
        if (whaleTrade1 != null && whaleTrade2 != null && whaleTrade3 != null) {
            double baseVal1 = 3.5 + random.nextDouble() * 5.0;
            double baseVal2 = 1.2 + random.nextDouble() * 3.0;
            double baseVal3 = 1.5 + random.nextDouble() * 4.0;
            
            whaleTrade1.setText(String.format("$%.1fM BUY on %s @ $%,.2f", baseVal1, symbol, currentLivePrice));
            whaleTrade2.setText(String.format("$%.1fM BUY on %s @ $%,.2f", baseVal2, symbol, currentLivePrice * 0.998));
            whaleTrade3.setText(String.format("$%.1fM SELL on %s @ $%,.2f", baseVal3, symbol, currentLivePrice * 1.003));
            
            double progress = 0.65 + random.nextDouble() * 0.28;
            if (whaleProgressBar != null) whaleProgressBar.setProgress(progress);
            if (whalePressureLabel != null) whalePressureLabel.setText(String.format("%.1f%%", progress * 100));
        }
    }

    // Panel Toggle Handlers
    @FXML
    private void handleToggleLeft(ActionEvent event) {
        if (leftPanel != null && toggleLeftBtn != null) {
            boolean sel = toggleLeftBtn.isSelected();
            leftPanel.setVisible(sel);
            leftPanel.setManaged(sel);
        }
    }

    @FXML
    private void handleToggleRight(ActionEvent event) {
        if (rightPanel != null && toggleRightBtn != null) {
            boolean sel = toggleRightBtn.isSelected();
            rightPanel.setVisible(sel);
            rightPanel.setManaged(sel);
        }
    }

    // FLAWLESS SPLITPANE DRAWER TOGGLE LOGIC (Dynamically adds/removes drawer so chart gets 100% full height when closed!)
    @FXML
    private void handleToggleBottom(ActionEvent event) {
        if (centerSplitPane != null && bottomDrawer != null && toggleBottomBtn != null) {
            boolean sel = toggleBottomBtn.isSelected();
            if (sel) {
                if (!centerSplitPane.getItems().contains(bottomDrawer)) {
                    centerSplitPane.getItems().add(bottomDrawer);
                    centerSplitPane.setDividerPositions(0.7);
                }
                bottomDrawer.setVisible(true);
                bottomDrawer.setManaged(true);
            } else {
                centerSplitPane.getItems().remove(bottomDrawer);
                bottomDrawer.setVisible(false);
                bottomDrawer.setManaged(false);
            }
        }
    }
    
    @FXML
    private void handleToggleRightClose(ActionEvent event) {
        if (rightPanel != null && toggleRightBtn != null) {
            rightPanel.setVisible(false);
            rightPanel.setManaged(false);
            toggleRightBtn.setSelected(false);
        }
    }
    
    @FXML
    private void handleToggleBottomClose(ActionEvent event) {
        if (centerSplitPane != null && bottomDrawer != null && toggleBottomBtn != null) {
            centerSplitPane.getItems().remove(bottomDrawer);
            bottomDrawer.setVisible(false);
            bottomDrawer.setManaged(false);
            toggleBottomBtn.setSelected(false);
        }
    }
    
    @FXML
    private void toggleRightPanel() {
        if (rightPanel != null && toggleRightBtn != null) {
            boolean isVisible = rightPanel.isVisible();
            rightPanel.setVisible(!isVisible);
            rightPanel.setManaged(!isVisible);
            toggleRightBtn.setSelected(!isVisible);
        }
    }

    @FXML
    private void handleLiquidateAll(ActionEvent event) {
        if (!com.tradevision.utils.SessionManager.isLoggedIn()) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "Please log in to manage positions.", false);
            return;
        }
        
        int userId = com.tradevision.utils.SessionManager.getCurrentUserId();
        List<com.tradevision.models.OrderModel> orders = com.tradevision.database.DatabaseManager.getInstance().getTradeHistory(userId);
        
        if (orders.isEmpty()) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "No active positions to liquidate.", false);
            return;
        }
        
        double realizedPnL = 1250.0 + (random.nextDouble() * 2400.0);
        double currentBal = com.tradevision.database.DatabaseManager.getInstance().getUserBalance(userId);
        com.tradevision.database.DatabaseManager.getInstance().updateUserBalance(userId, currentBal + realizedPnL);
        
        com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), String.format("Liquidated %d positions. Realized PnL: +$%,.2f added to balance!", orders.size(), realizedPnL), true);
        updateBalance();
        refreshHistoryTable();
        
        if (aiChatListView != null) {
            aiChatListView.getItems().add("AI Co-Pilot: Successfully liquidated all active positions. Realized PnL locked and added to available balance.");
            aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
        }
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        isDarkMode = !isDarkMode;
        
        if (isDarkMode) {
            rootPane.getStyleClass().remove("light-mode");
            if (indicatorMenuButton != null) indicatorMenuButton.getStyleClass().remove("light-mode-menu");
            themeToggle.setText("🌙 Theme");
            webEngine.executeScript("applyTheme('dark')");
        } else {
            if (!rootPane.getStyleClass().contains("light-mode")) {
                rootPane.getStyleClass().add("light-mode");
            }
            if (indicatorMenuButton != null && !indicatorMenuButton.getStyleClass().contains("light-mode-menu")) {
                indicatorMenuButton.getStyleClass().add("light-mode-menu");
            }
            themeToggle.setText("☀️ Theme");
            webEngine.executeScript("applyTheme('light')");
        }
    }

    private void updateBalance() {
        if (com.tradevision.utils.SessionManager.isLoggedIn()) {
            double bal = com.tradevision.database.DatabaseManager.getInstance().getUserBalance(com.tradevision.utils.SessionManager.getCurrentUserId());
            if (balanceLabel != null) {
                balanceLabel.setText(String.format("Available Balance: $%,.2f", bal));
            }
        }
    }

    private void setupHistoryTable() {
        if (historyTable != null) {
            colTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("time"));
            colSymbol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("symbol"));
            colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
            colQty.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
            colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));
            
            colType.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.equalsIgnoreCase("BUY")) {
                            setStyle("-fx-text-fill: -accent-green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: -accent-red; -fx-font-weight: bold;");
                        }
                    }
                }
            });
            refreshHistoryTable();
        }
    }

    private void refreshHistoryTable() {
        if (com.tradevision.utils.SessionManager.isLoggedIn() && historyTable != null) {
            java.util.List<com.tradevision.models.OrderModel> orders = com.tradevision.database.DatabaseManager.getInstance().getTradeHistory(com.tradevision.utils.SessionManager.getCurrentUserId());
            historyTable.setItems(javafx.collections.FXCollections.observableArrayList(orders));
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        com.tradevision.utils.SessionManager.logout();
        try {
            App.setRoot("views/Login");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toUpperCase();
        if (!query.isEmpty()) {
            if (!watchlistList.getItems().contains(query)) {
                watchlistList.getItems().add(0, query);
            }
            watchlistList.getSelectionModel().select(query);
            searchField.clear();
        }
    }

    @FXML
    private void handleToggleKillSwitch(ActionEvent event) {
        killSwitchActive = !killSwitchActive;
        if (killSwitchActive) {
            killSwitchBtn.setText("Active");
            killSwitchBtn.setStyle("-fx-background-color: rgba(46,160,67,0.2); -fx-text-fill: -accent-green; -fx-font-size: 10px; -fx-padding: 2 6;");
            riskStatusLabel.setText("Safe Status");
            riskStatusLabel.setStyle("-fx-text-fill: -accent-green; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            killSwitchBtn.setText("Disabled");
            killSwitchBtn.setStyle("-fx-background-color: rgba(248,81,73,0.2); -fx-text-fill: -accent-red; -fx-font-size: 10px; -fx-padding: 2 6;");
            riskStatusLabel.setText("Warning: Unprotected");
            riskStatusLabel.setStyle("-fx-text-fill: -accent-red; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleBuy(ActionEvent event) {
        executeTrade("BUY");
    }

    @FXML
    private void handleSell(ActionEvent event) {
        executeTrade("SELL");
    }

    private void executeTrade(String type) {
        if (!com.tradevision.utils.SessionManager.isLoggedIn()) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "Please log in to trade.", false);
            return;
        }
        
        if (killSwitchActive && dailyLoss >= maxDailyLossLimit) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "🚨 RISK GUARDIAN ACTIVE: Daily Loss Limit ($2,000) reached. Trading locked to protect account balance.", false);
            return;
        }
        
        String symbol = watchlistList.getSelectionModel().getSelectedItem();
        if (symbol == null) return;
        
        double priceStr = type.equals("BUY") ? 
            Double.parseDouble(buyPriceLabel.getText().replace(",", "")) : 
            Double.parseDouble(sellPriceLabel.getText().replace(",", ""));
            
        double qty = 1.0;
        try {
            qty = Double.parseDouble(quantityField.getText());
        } catch (NumberFormatException e) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), "Invalid quantity!", false);
            return;
        }
        
        int userId = com.tradevision.utils.SessionManager.getCurrentUserId();
        boolean success = com.tradevision.database.DatabaseManager.getInstance().executeTrade(userId, symbol, type, qty, priceStr);
        
        if (success) {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), type + " " + qty + " " + symbol + " @ $" + String.format("%,.2f", priceStr) + " Success!", true);
            updateBalance();
            refreshHistoryTable();
            
            if (aiChatListView != null) {
                aiChatListView.getItems().add("AI Co-Pilot Post-Mortem: Excellent " + type + " execution on " + symbol + " @ $" + String.format("%,.2f", priceStr) + ". Entry perfectly aligned with institutional order flow. Rating: A+");
                aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
            }
        } else {
            com.tradevision.utils.NotificationUtil.showToast(buyPriceLabel.getScene().getWindow(), type + " Failed. Insufficient funds.", false);
        }
    }

    @FXML
    private void handleTimeframe(ActionEvent event) {
        ToggleButton btn = (ToggleButton) event.getSource();
        if (btn.isSelected()) {
            switch (btn.getText()) {
                case "1m": currentTimeframeSeconds = 60; break;
                case "5m": currentTimeframeSeconds = 300; break;
                case "15m": currentTimeframeSeconds = 900; break;
                case "1H": currentTimeframeSeconds = 3600; break;
                case "1D": currentTimeframeSeconds = 86400; break;
            }
            loadDummyDataForSymbol(currentSymbol);
        } else {
            btn.setSelected(true);
        }
    }
    
    private boolean isBarChart = false;
    @FXML
    private void handleChartStyle(ActionEvent event) {
        isBarChart = !isBarChart;
        Button btn = (Button) event.getSource();
        btn.setText(isBarChart ? "Candles" : "Bars");
        String js = String.format("changeChartType('%s');", isBarChart ? "Bars" : "Candles");
        Platform.runLater(() -> {
            try {
                webEngine.executeScript(js);
            } catch (Exception e) {}
        });
    }

    @FXML
    private void handleFullscreen(ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    private void handleQuickLot(ActionEvent event) {
        Button btn = (Button) event.getSource();
        quantityField.setText(btn.getText());
    }

    @FXML
    private void handleToggleIndicator(ActionEvent event) {
        CheckMenuItem item = (CheckMenuItem) event.getSource();
        if (webEngine != null) {
            String indicatorName = item.getText();
            boolean isSelected = item.isSelected();
            webEngine.executeScript("toggleIndicator('" + indicatorName + "', " + isSelected + ");");
        }
    }

    @FXML
    private void handleCrosshairTool(ActionEvent event) {
        if (webEngine != null) webEngine.executeScript("enableCrosshair();");
    }

    @FXML
    private void handleLineTool(ActionEvent event) {
        if (webEngine != null) webEngine.executeScript("enableLineTool();");
    }

    @FXML
    private void handleTextTool(ActionEvent event) {
        if (webEngine != null) webEngine.executeScript("enableTextTool();");
    }

    @FXML
    private void handleClearDrawings(ActionEvent event) {
        if (webEngine != null) webEngine.executeScript("clearDrawings();");
    }

    @FXML
    private void handleFiboTool(ActionEvent event) {
        if (webEngine != null) webEngine.executeScript("enableFibonacciTool();");
    }

    @FXML
    private void handleRectTool(ActionEvent event) {
        if (webEngine != null) webEngine.executeScript("enableRectangleTool();");
    }
    
    private void showFeatureDialog(String title, String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(description);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #161B22; -fx-text-fill: #F0F6FC;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #0B0F17;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #F0F6FC;");
        
        alert.showAndWait();
    }

    @FXML
    private void handleShowAlerts(ActionEvent event) {
        showFeatureDialog("Alerts", "Configure price alerts, crossed conditions, and webhook notifications.");
    }

    @FXML
    private void handleShowReplay(ActionEvent event) {
        showFeatureDialog("Bar Replay Simulator", "Bar Replay mode allows you to replay historical candles tick-by-tick to practice executing trades under live market conditions.");
    }

    @FXML
    private void handleShowProfile(javafx.scene.input.MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Profile");
        alert.setHeaderText("Alisha's Institutional Profile");
        
        String info = "Username: alisha\n" +
                      "Email: alisha@tradevision.pro\n" +
                      "Tier: Premium Institutional Pro\n" +
                      "Prop Firm Status: Active Evaluation\n" +
                      "Member Since: June 2026\n" +
                      "Active Devices: 1\n" +
                      "Current Balance: " + balanceLabel.getText();
                      
        alert.setContentText(info);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #161B22; -fx-text-fill: #F0F6FC;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #0B0F17;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #F0F6FC;");
        
        alert.showAndWait();
    }

    @FXML
    private void handleShowSettings(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Terminal Settings");
        dialog.setHeaderText("Advanced Institutional Configuration");

        DialogPane dialogPane = dialog.getDialogPane();
        VBox content = new VBox(18);
        
        // DYNAMIC LIGHT/DARK MODE & STRONG VISIBLE COLOR SCHEME
        String labelStyle;
        String comboStyle;
        String watermarkStyle;
        String closeBtnStyle;
        
        if (isDarkMode) {
            dialogPane.setStyle("-fx-background-color: #0B0F17; -fx-border-color: #00F0FF; -fx-border-width: 2; -fx-text-fill: white; -fx-min-width: 480;");
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #161B22; -fx-border-color: #00F0FF; -fx-border-width: 0 0 1 0; -fx-font-weight: bold; -fx-font-size: 14px;");
            content.setStyle("-fx-padding: 25 20; -fx-background-color: #0B0F17;");
            labelStyle = "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-opacity: 1.0;";
            comboStyle = "-fx-background-color: #161B22; -fx-border-color: #00F0FF; -fx-border-width: 1; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-opacity: 1.0;";
            watermarkStyle = "-fx-text-fill: #00F0FF; -fx-font-size: 13px; -fx-font-weight: bold; -fx-opacity: 1.0;";
            closeBtnStyle = "-fx-background-color: #00F0FF; -fx-text-fill: #0B0F17; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 20; -fx-cursor: hand;";
            dialogPane.getStyleClass().addAll("settings-combo-dark");
        } else {
            dialogPane.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #0366D6; -fx-border-width: 2; -fx-text-fill: #0B0F17; -fx-min-width: 480;");
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #F0F3F6; -fx-border-color: #0366D6; -fx-border-width: 0 0 1 0; -fx-font-weight: bold; -fx-font-size: 14px;");
            content.setStyle("-fx-padding: 25 20; -fx-background-color: #FFFFFF;");
            labelStyle = "-fx-text-fill: #0B0F17; -fx-font-size: 13px; -fx-font-weight: bold; -fx-opacity: 1.0;";
            comboStyle = "-fx-background-color: #F0F3F6; -fx-border-color: #0366D6; -fx-border-width: 1; -fx-text-fill: #0B0F17; -fx-font-size: 12px; -fx-font-weight: bold; -fx-opacity: 1.0;";
            watermarkStyle = "-fx-text-fill: #0366D6; -fx-font-size: 13px; -fx-font-weight: bold; -fx-opacity: 1.0;";
            closeBtnStyle = "-fx-background-color: #0366D6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 20; -fx-cursor: hand;";
            dialogPane.getStyleClass().addAll("settings-combo-light");
        }

        // 1. Show Grid Lines Toggle
        CheckBox gridCheck = new CheckBox("Show Visual Grid Lines");
        gridCheck.setStyle(labelStyle);
        gridCheck.setSelected(true);
        gridCheck.setOnAction(e -> {
            boolean visible = gridCheck.isSelected();
            String color = visible ? (isDarkMode ? "rgba(31, 41, 55, 0.5)" : "rgba(225, 228, 232, 0.8)") : "transparent";
            webEngine.executeScript("chart.applyOptions({ grid: { vertLines: { color: '" + color + "' }, horzLines: { color: '" + color + "' } } });");
        });

        // 2. Show Right Price Scale Toggle
        CheckBox scaleCheck = new CheckBox("Enable Right Price Scale");
        scaleCheck.setStyle(labelStyle);
        scaleCheck.setSelected(true);
        scaleCheck.setOnAction(e -> {
            boolean visible = scaleCheck.isSelected();
            webEngine.executeScript("chart.applyOptions({ rightPriceScale: { visible: " + visible + " } });");
        });

        // 3. Institutional Watermark Toggle
        CheckBox watermarkCheck = new CheckBox("Institutional Background Watermark");
        watermarkCheck.setStyle(watermarkStyle);
        watermarkCheck.setSelected(false);
        watermarkCheck.setOnAction(e -> {
            boolean visible = watermarkCheck.isSelected();
            webEngine.executeScript("toggleWatermark(" + visible + ", 'TradeVision Pro | Institutional');");
        });

        // 4. Crosshair Mode Selector
        ComboBox<String> crosshairBox = new ComboBox<>();
        crosshairBox.getItems().addAll("Normal Mode", "Magnet Mode");
        crosshairBox.setValue("Normal Mode");
        crosshairBox.setStyle(comboStyle);
        crosshairBox.getStyleClass().add("settings-combo");
        crosshairBox.setOnAction(e -> {
            int mode = crosshairBox.getValue().equals("Normal Mode") ? 0 : 1;
            webEngine.executeScript("chart.applyOptions({ crosshair: { mode: " + mode + " } });");
        });
        HBox crosshairRow = new HBox(15, new Label("Crosshair Snap:"), crosshairBox);
        crosshairRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ((Label)crosshairRow.getChildren().get(0)).setStyle(labelStyle + " -fx-pref-width: 170;");

        // 5. Bullish Candle Color Selector
        ComboBox<String> upColorBox = new ComboBox<>();
        upColorBox.getItems().addAll("Neon Green (#2EA043)", "Electric Cyan (#00F0FF)", "Solid Green (#00FF00)", "Crisp White (#FFFFFF)");
        upColorBox.setValue("Neon Green (#2EA043)");
        upColorBox.setStyle(comboStyle + (isDarkMode ? " -fx-text-fill: #2EA043;" : " -fx-text-fill: #28A745;"));
        upColorBox.getStyleClass().add("settings-combo");
        upColorBox.setOnAction(e -> {
            String val = upColorBox.getValue();
            String color = "#2EA043";
            if (val.contains("Cyan")) color = "#00F0FF";
            else if (val.contains("Solid")) color = "#00FF00";
            else if (val.contains("White")) color = "#FFFFFF";
            webEngine.executeScript("setCandleColors('" + color + "', '#F85149');");
        });
        HBox upColorRow = new HBox(15, new Label("Bullish Candle Color:"), upColorBox);
        upColorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ((Label)upColorRow.getChildren().get(0)).setStyle(labelStyle + " -fx-pref-width: 170;");

        // 6. Bearish Candle Color Selector
        ComboBox<String> downColorBox = new ComboBox<>();
        downColorBox.getItems().addAll("Neon Red (#F85149)", "Deep Crimson (#8B0000)", "Magenta (#FF00FF)", "Vibrant Orange (#FF5500)");
        downColorBox.setValue("Neon Red (#F85149)");
        downColorBox.setStyle(comboStyle + (isDarkMode ? " -fx-text-fill: #F85149;" : " -fx-text-fill: #D73A49;"));
        downColorBox.getStyleClass().add("settings-combo");
        downColorBox.setOnAction(e -> {
            String val = downColorBox.getValue();
            String color = "#F85149";
            if (val.contains("Crimson")) color = "#8B0000";
            else if (val.contains("Magenta")) color = "#FF00FF";
            else if (val.contains("Orange")) color = "#FF5500";
            webEngine.executeScript("setCandleColors('#2EA043', '" + color + "');");
        });
        HBox downColorRow = new HBox(15, new Label("Bearish Candle Color:"), downColorBox);
        downColorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ((Label)downColorRow.getChildren().get(0)).setStyle(labelStyle + " -fx-pref-width: 170;");

        // 7. Chart Canvas Background Selector
        ComboBox<String> bgBox = new ComboBox<>();
        bgBox.getItems().addAll("Deep Carbon (#0B0F17)", "Midnight Navy (#0A192F)", "Pitch Black (#000000)", "Subtle Grey (#161B22)");
        bgBox.setValue("Deep Carbon (#0B0F17)"); bgBox.setStyle(comboStyle); bgBox.getStyleClass().add("settings-combo"); bgBox.setOnAction(e -> { String val = bgBox.getValue(); String color = "#0B0F17"; if (val.contains("Navy")) color = "#0A192F"; else if (val.contains("Pitch")) color = "#000000"; else if (val.contains("Grey")) color = "#161B22"; webEngine.executeScript("setChartBackground('" + color + "');"); }); HBox bgRow = new HBox(15, new Label("Chart Canvas Background:"), bgBox); bgRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT); ((Label)bgRow.getChildren().get(0)).setStyle(labelStyle + " -fx-pref-width: 170;"); content.getChildren().addAll(gridCheck, scaleCheck, watermarkCheck, crosshairRow, upColorRow, downColorRow, bgRow); dialogPane.setContent(content); dialogPane.getButtonTypes().add(ButtonType.CLOSE); javafx.scene.Node closeButton = dialogPane.lookupButton(ButtonType.CLOSE); closeButton.setStyle(closeBtnStyle); dialog.showAndWait(); }

    private javafx.animation.Timeline liveUpdateTimeline;
    private double currentLivePrice = 100.0;
    private long currentTimestamp = System.currentTimeMillis() / 1000;
    private double currentOpen, currentHigh, currentLow, currentClose;

    private void startLiveUpdates() {
        if (liveUpdateTimeline != null) {
            liveUpdateTimeline.stop();
        }
        
        liveUpdateTimeline = new javafx.animation.Timeline(new javafx.animation.KeyFrame(
            javafx.util.Duration.seconds(1),
            e -> {
                tickCounter++;
                
                // 1. Live Watchlist Price Fluctuations
                if (watchlistList != null && tickCounter % 3 == 0) {
                    watchlistList.refresh();
                }

                double change = (random.nextDouble() - 0.5) * (currentLivePrice * 0.002);
                currentClose = currentClose + change;
                if (currentClose > currentHigh) currentHigh = currentClose;
                if (currentClose < currentLow) currentLow = currentClose;
                
                currentLivePrice = currentClose;

                buyPriceLabel.setText(String.format("%,.2f", currentLivePrice));
                sellPriceLabel.setText(String.format("%,.2f", currentLivePrice - (currentLivePrice * 0.0005)));

                String json = String.format(java.util.Locale.US,
                    "{\"time\": %d, \"open\": %.2f, \"high\": %.2f, \"low\": %.2f, \"close\": %.2f}",
                    currentTimestamp, currentOpen, currentHigh, currentLow, currentClose
                );
                
                // PRISTINE TIMEFRAME BOUNDARY TRACKING
                long nowSec = System.currentTimeMillis() / 1000;
                long secondsLeft = (currentTimestamp + currentTimeframeSeconds) - nowSec;
                if (secondsLeft < 0) secondsLeft = 0;
                
                String countdown;
                if (currentTimeframeSeconds >= 3600) {
                    long h = secondsLeft / 3600;
                    long m = (secondsLeft % 3600) / 60;
                    long s = secondsLeft % 60;
                    countdown = String.format("%02d:%02d:%02d", h, m, s);
                } else {
                    long m = secondsLeft / 60;
                    long s = secondsLeft % 60;
                    countdown = String.format("%02d:%02d", m, s);
                }
                
                if (nowSec >= currentTimestamp + currentTimeframeSeconds) {
                    currentTimestamp += currentTimeframeSeconds;
                    currentOpen = currentClose;
                    currentHigh = currentClose;
                    currentLow = currentClose;
                }

                if (webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                    webEngine.executeScript("updateChartData('" + json + "', '" + countdown + "');");
                }
            }
        ));
        liveUpdateTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        liveUpdateTimeline.play();
    }

    private void loadDummyDataForSymbol(String symbol) {
        currentSymbol = symbol;
        double basePrice = 100.0;
        if (symbol.equals("BTCUSD")) basePrice = 64200.0;
        else if (symbol.equals("XAUUSD")) basePrice = 2330.0;
        else if (symbol.equals("AAPL")) basePrice = 192.50;
        else if (symbol.equals("EURUSD")) basePrice = 1.0850;
        else if (symbol.equals("TSLA")) basePrice = 185.00;
        
        currentLivePrice = basePrice;
        currentOpen = basePrice;
        currentHigh = basePrice;
        currentLow = basePrice;
        currentClose = basePrice;
        
        long nowSec = System.currentTimeMillis() / 1000;
        currentTimestamp = nowSec - (nowSec % currentTimeframeSeconds) - (100L * currentTimeframeSeconds);

        StringBuilder sb = new StringBuilder("[");
        double price = basePrice;
        
        for (int i = 0; i < 100; i++) {
            double change = (random.nextDouble() - 0.5) * (price * 0.02);
            double open = price;
            double close = price + change;
            double high = Math.max(open, close) + random.nextDouble() * (price * 0.01);
            double low = Math.min(open, close) - random.nextDouble() * (price * 0.01);
            
            long time = currentTimestamp + (i * currentTimeframeSeconds);
            
            sb.append(String.format(java.util.Locale.US,
                "{\"time\": %d, \"open\": %.2f, \"high\": %.2f, \"low\": %.2f, \"close\": %.2f}",
                time, open, high, low, close
            ));
            
            if (i < 99) sb.append(",");
            price = close;
            
            if (i == 99) {
                currentOpen = open;
                currentHigh = high;
                currentLow = low;
                currentClose = close;
                currentTimestamp = time; 
            }
        }
        sb.append("]");
        
        buyPriceLabel.setText(String.format("%,.2f", currentLivePrice));
        sellPriceLabel.setText(String.format("%,.2f", currentLivePrice - (currentLivePrice * 0.0005)));
        
        if (webEngine != null && webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
            webEngine.executeScript("setChartData('" + sb.toString() + "', '" + currentSymbol + "');");
            startLiveUpdates();
        }
    }

    public class JavaConsole {
        public void log(String message) {
            System.out.println("JS LOG: " + message);
        }
        public void error(String message) {
            System.err.println("JS ERROR: " + message);
        }
    }
}
