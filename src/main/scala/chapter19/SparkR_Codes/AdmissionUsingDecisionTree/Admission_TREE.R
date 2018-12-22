install.packages("rpart")
install.packages("rattle")
install.packages("rpart.plot")
install.packages("RColorBrewer")
library(rpart)
library(rattle)
library(rpart.plot)
library(RColorBrewer)
data<- read.csv("C:/Users/rezkar/Google Drive/Gre_Coll_Adm.csv")
str(data)
View(data)
adm_data<-as.data.frame(data)

### Creating the tree 
tree <- rpart(Admission_YN ~ adm_data$Grad_Rec_Exam + adm_data$Grad_Per+ adm_data$Rank_of_col, data=adm_data, method="class")
plot(tree)
text(tree, pretty=0)


###########Plotting the tree using the Rattle package 
rattle()
fancyRpartPlot(tree)

printcp(tree)
rpart(formula = Admission_YN ~ adm_data$Grad_Rec_Exam + adm_data$Grad_Per + adm_data$Rank_of_col, data = adm_data, method = "class")
plotcp(tree)


##### Prune the tree to create an optimal decision tree :
ptree<- prune(tree, cp= tree$cptable[which.min(tree$cptable[,"xerror"]),"CP"])
fancyRpartPlot(ptree, uniform=TRUE, main="Pruned Classification Tree")

