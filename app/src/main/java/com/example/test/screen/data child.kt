package com.example.test.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ChildEntryScreen(
    navController: NavController,
    parentId: String,
    onAddChild: (List<String>) -> Unit,
    onDeleteChild: (String) -> Unit,
    childDataList: List<List<String>>
) {
    var newModelCerdas by remember { mutableStateOf("") }
    val noChildEntries = childDataList.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Child Entry for Parent ID: $parentId", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Input field for adding a child entry
        OutlinedTextField(
            value = newModelCerdas,
            onValueChange = { newModelCerdas = it },
            label = { Text("Model Cerdas") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons for adding, deleting, and returning
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (newModelCerdas.isNotBlank()) {
                    val newId = "$parentId-${'A' + childDataList.size}" // Generate new ID for child
                    val status = "Draft"
                    onAddChild(listOf(newId, newModelCerdas, status)) // Add the new child
                    newModelCerdas = "" // Reset input field
                }
            }) {
                Text("Add Child Entry")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Return")
            }

            Button(onClick = {
                // Delete the last child if available
                if (childDataList.isNotEmpty()) {
                    val lastChildId = childDataList.last()[0] // Get the last child's ID
                    onDeleteChild(lastChildId) // Delete it
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Delete")
            }

            Button(onClick = {
                // Implement save functionality here
            }) {
                Text("Save Changes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display child entries if available
        Text("Child Entries for Parent ID $parentId:")
        if (noChildEntries) {
            Text("No child entries available.")
        } else {
            // Display each child entry row
            Column(modifier = Modifier.fillMaxWidth()) {
                childDataList.forEach { child ->
                    ChildEntryRow(child = child)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate back to the previous screen
        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Form")
        }
    }
}

@Composable
fun ChildEntryRow(child: List<String>) {
    // Display each child entry in a row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.Gray), // Adding border for visualization
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "ID: ${child[0]}", modifier = Modifier.weight(1f))
        Text(text = "Model: ${child[1]}", modifier = Modifier.weight(1f))
        Text(text = "Status: ${child[2]}", modifier = Modifier.weight(1f))
    }
}
