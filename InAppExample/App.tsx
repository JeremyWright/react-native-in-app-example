import React from 'react';
import { NativeModules } from 'react-native';
import { Button, View, StyleSheet, TouchableOpacity, Text } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import uuid from 'react-native-uuid';

const { InAppModule } = NativeModules;
const Stack = createNativeStackNavigator();
var conversationId = null;

const onConfigure = () => {
  conversationId = uuid.v4();
  InAppModule.configure(

  "https://smi238.test1.my.pc-rnd.salesforce-scrt.com",
  "00DSG0000003vL7",
  "Hidden_PreChat",

    conversationId
  );
};

const onLaunch = () => {
  InAppModule.launch();
};

const onConversations = () => {
  InAppModule.retrieveConversations(
    result => {
      console.log(`Conversation Result: ${result}`)
    }
  );
};

const onDestroyDB = () => {
  InAppModule.destroyDB();
}

const HomeScreen = ({navigation}) => {
  return (
    <View style={styles.container}>
      <Button
        title="Click here to Initialize or to create new conversations"
        color="#841584"
        onPress={onConfigure}
      />
      <Button
        title="Launch Chat"
        color="#841584"
        onPress={onLaunch}
      />
      <Button
        title="Log Conversation Info"
        color="#841584"
        onPress={onConversations}
      />
      <Button
        title="Wipe offline Data"
        color="#841584"
        onPress={onDestroyDB}
      />
    </View>
  );
}

const App = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
				<Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{title: 'Welcome'}}
        	/>
      </Stack.Navigator>
    </NavigationContainer>
  );
};


const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'white'
  },
  button: {
    backgroundColor: 'blue',
    padding: 10,
    borderRadius: 5,
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
  },
});

export default App;
