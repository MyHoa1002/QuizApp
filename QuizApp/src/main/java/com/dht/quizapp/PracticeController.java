/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.dht.quizapp;

import com.dht.pojo.Category;
import com.dht.pojo.Level;
import com.dht.pojo.Question;
import com.dht.services.questions.BaseQuestionServices;
import com.dht.services.questions.CategoryQuestionServicesDecorator;
import com.dht.services.questions.LevelQuestionServicesDecorator;
import com.dht.services.questions.LimitQuestionServicesDecorator;
import com.dht.services.questions.QuestionServices;
import com.dht.utils.Configs;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class PracticeController implements Initializable {
    @FXML private TextField txtNum;
    @FXML private VBox vboxChoices;
    @FXML private Text txtContent;
    @FXML private Text txtResut;
    @FXML private ComboBox<Category> cbSearchCates;
    @FXML private ComboBox<com.dht.pojo.Level> cbSearchLevels;
    private List<Question> questions;
    private int currentQuestion = 0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.cbSearchCates.setItems(FXCollections.observableList(Configs.cateServices.getCates()));
            this.cbSearchLevels.setItems(FXCollections.observableList(Configs.levelServices.getLevels()));
            
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }    
    
    public void handleStart(ActionEvent event) {
        try {
            BaseQuestionServices s = Configs.questionServices;
            
            Category c = this.cbSearchCates.getSelectionModel().getSelectedItem();
            if (c != null)
                s = new CategoryQuestionServicesDecorator(s, c);
            
            Level l = this.cbSearchLevels.getSelectionModel().getSelectedItem();
            if (l != null)
                s = new LevelQuestionServicesDecorator(s, l);
            
            s = new LimitQuestionServicesDecorator(s, Integer.parseInt(this.txtNum.getText()));
        
            this.questions = s.list();
            loadQuestion();
        } catch (SQLException ex) {
            
        }
    }
    
    public void handleNext(ActionEvent event) {
        this.txtResut.setText("");
        if (this.currentQuestion < this.questions.size() - 1) {
            this.currentQuestion++;
            this.loadQuestion();
        }
    }
    public void handleCheck(ActionEvent event) {
        Question q = this.questions.get(this.currentQuestion);
        for (int i = 0; i < q.getChoices().size(); i++)
            if (q.getChoices().get(i).isCorrect()) {
                RadioButton rdo = (RadioButton)this.vboxChoices.getChildren().get(i);
                
                this.txtResut.getStyleClass().clear();
                if (rdo.isSelected()) {
                    this.txtResut.setText("Congratulation! exactly!");
                    this.txtResut.getStyleClass().add("Correct");
                } else {
                    this.txtResut.setText("So sorry! wrongly!");
                    this.txtResut.getStyleClass().add("Wrong");
                }
                
                break;
            }
    }
    
    private void loadQuestion() {
        Question q = this.questions.get(this.currentQuestion);
        this.txtContent.setText(q.getContent());
        
        this.vboxChoices.getChildren().clear();
        ToggleGroup t = new ToggleGroup();
        for (var c: q.getChoices()) {
            RadioButton r = new RadioButton(c.getContent());
            r.setToggleGroup(t);
            this.vboxChoices.getChildren().add(r);
        }
    }
}
