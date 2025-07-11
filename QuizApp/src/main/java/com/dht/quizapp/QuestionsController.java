/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.dht.quizapp;

import com.dht.pojo.Category;
import com.dht.pojo.Choice;
import com.dht.pojo.Level;
import com.dht.pojo.Question;
import com.dht.services.CategoryServices;
import com.dht.services.FlyweightFactory;
import com.dht.services.LevelServices;
import com.dht.services.questions.BaseQuestionServices;
import com.dht.services.questions.CategoryQuestionServicesDecorator;
import com.dht.services.questions.KeywordQuestionServicesDecorator;
import com.dht.services.questions.LevelQuestionServicesDecorator;
import com.dht.services.questions.QuestionServices;
import com.dht.utils.Configs;
import com.dht.utils.JdbcConnector;
import com.dht.utils.MyAlert;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class QuestionsController implements Initializable {
    @FXML private TextArea txtContent;
    @FXML private ComboBox<Category> cbCates;
    @FXML private ComboBox<Category> cbSearchCates;
    @FXML private ComboBox<Level> cbLevels;
    @FXML private ComboBox<Level> cbSearchLevels;
    @FXML private TableView<Question> tbQuestions;
    @FXML private VBox vboxChoices;
    @FXML private TextField txtSearch;
    @FXML private ToggleGroup toggleChoice;
    

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            
            this.cbCates.setItems(FXCollections.observableList(FlyweightFactory.getData(Configs.cateServices, "categories")));
            this.cbLevels.setItems(FXCollections.observableList(FlyweightFactory.getData(Configs.levelServices, "levels")));
            
            this.cbSearchCates.setItems(FXCollections.observableList(FlyweightFactory.getData(Configs.cateServices, "categories")));
            this.cbSearchLevels.setItems(FXCollections.observableList(FlyweightFactory.getData(Configs.levelServices, "levels")));
            
            this.loadColumns();
            this.loadQuestion(Configs.questionServices.list());
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        
        this.txtSearch.textProperty().addListener((e) -> {
            try {
                BaseQuestionServices s = new KeywordQuestionServicesDecorator(Configs.questionServices, this.txtSearch.getText());
                this.loadQuestion(s.list());
            } catch (SQLException ex) {
                Logger.getLogger(QuestionsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        });
        
        this.cbSearchCates.getSelectionModel().selectedItemProperty().addListener(e -> {
            try {
                BaseQuestionServices s = new CategoryQuestionServicesDecorator(Configs.questionServices, this.cbSearchCates.getSelectionModel().getSelectedItem());
                this.loadQuestion(s.list());
            } catch (SQLException ex) {
                Logger.getLogger(QuestionsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        });
        
        this.cbSearchLevels.getSelectionModel().selectedItemProperty().addListener(e -> {
            try {
                BaseQuestionServices s = new LevelQuestionServicesDecorator(Configs.questionServices, this.cbSearchLevels.getSelectionModel().getSelectedItem());
                this.loadQuestion(s.list());
            } catch (SQLException ex) {
                Logger.getLogger(QuestionsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        });
    }  
    
    public void addChoice(ActionEvent event) {
        HBox h = new HBox();
        h.getStyleClass().add("Main");
        
        RadioButton r = new RadioButton();
        r.setToggleGroup(toggleChoice);
        
        TextField txt = new TextField();
        txt.getStyleClass().add("Input");
        h.getChildren().addAll(r, txt);
        
        this.vboxChoices.getChildren().add(h);
    }
    
    public void addQuestion(ActionEvent event) {
        try {
            Question.Builder b = new Question.Builder(this.txtContent.getText(),
                    this.cbCates.getSelectionModel().getSelectedItem(),
                    this.cbLevels.getSelectionModel().getSelectedItem());
            
            for (var c: this.vboxChoices.getChildren()) {
                HBox h = (HBox) c;
                
                Choice choice = new Choice(((TextField)h.getChildren().get(1)).getText(), 
                            ((RadioButton)h.getChildren().get(0)).isSelected());
                b.addChoice(choice);
            }
            
            Configs.uQServices.addQuestion(b.build());
            MyAlert.getInstance().showMsg("Thêm câu hỏi thành công!");
            
            // Bổ sung
            this.tbQuestions.getItems().add(0, b.build());
        } catch (SQLException ex) {
            MyAlert.getInstance().showMsg("Thêm câu hỏi thất bại!");
        } catch (Exception ex) {
            MyAlert.getInstance().showMsg("Dữ liệu không hợp lệ!");
        }
    }
    
    private void loadColumns() {
        TableColumn colId = new TableColumn("Id");
        colId.setCellValueFactory(new PropertyValueFactory("id"));
        colId.setPrefWidth(100);
        
        TableColumn colContent = new TableColumn("Nội dung câu hỏi");
        colContent.setCellValueFactory(new PropertyValueFactory("content"));
        colContent.setPrefWidth(250);
        
        TableColumn colAction = new TableColumn();
        colAction.setCellFactory((e) -> {
            TableCell cell = new TableCell();
            
            Button btn = new Button("Xóa");
            btn.setOnAction((event) -> {
                Optional<ButtonType> t = MyAlert.getInstance().showMsg(
                        "Xóa câu hỏi thì các lựa chọn cũng bị xóa theo. Bạn chắc chắn không?", 
                        Alert.AlertType.CONFIRMATION);
                if (t.isPresent() && t.get().equals(ButtonType.OK)) {
                    Question q = (Question) cell.getTableRow().getItem();
                    try {
                        Configs.uQServices.deleteQuestion(q.getId());
                        
                        this.tbQuestions.getItems().remove(q);
                        MyAlert.getInstance().showMsg("Xóa thành công!");
                    } catch (SQLException ex) {
                        MyAlert.getInstance().showMsg("Xóa thất bại!", Alert.AlertType.WARNING);
                    }
                }
            });
            
            cell.setGraphic(btn);
            
            return cell;
        });
        
        
        this.tbQuestions.getColumns().addAll(colId, colContent, colAction);
    }
    
    private void loadQuestion(List<Question> questions) {
         this.tbQuestions.setItems(FXCollections.observableList(questions));
    }
}
