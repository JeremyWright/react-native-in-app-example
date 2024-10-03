//
//  RCTInAppModule.swift
//  InAppExample
//
//  Created by Jeremy Wright on 2023-08-17.
//

import Foundation
import SMIClientUI
import SMIClientCore

@available(iOS 14.1, *)
@objc(InAppModule)
class InAppModule: NSObject {
  @objc static func requiresMainQueueSetup() -> Bool { return true }

  var interface: InterfaceViewController?
  var config: UIConfiguration?
  var core: CoreClient?

  @objc func configure(_ url: String, organizationId: String, developerName: String, conversationId: String) {
    // Attempt to resolve the URL, if the url string is invalid this will return early.
    guard let concreteURL: URL = URL(string: url) else { return }

    // Attempt to compose the provided conversation id to a UUID
    guard let uuid: UUID = UUID(uuidString: conversationId) else { return }

    let config = UIConfiguration(serviceAPI: concreteURL, organizationId: organizationId, developerName: developerName, conversationId: uuid)
    self.config = config
    self.core = CoreFactory.create(withConfig: config)

    // add the module as a delegate listener
    core?.addDelegate(delegate: self, queue: .main)
  }

  @objc func launch() {
    guard let config = self.config else { return }

    DispatchQueue.main.async {
      // For a modal presentation which may be easier to utilize in React apps which don't have a native navigation stack please
      // see the following:
      guard let controller = UIApplication.shared.windows.filter({$0.isKeyWindow}).first?.rootViewController else { return }

      let interface = InterfaceViewController(config)

      let navController = UINavigationController(rootViewController: interface)

      navController.navigationBar.backgroundColor = .systemBackground
      navController.modalPresentationStyle = .overCurrentContext
      interface.navigationItem.leftBarButtonItem = UIBarButtonItem(title: "Back", style: .plain, target: self, action: #selector(self.dismiss))

      controller.present(navController, animated: true)
    }
  }

  @objc func dismiss() {
    guard let controller = UIApplication.shared.windows.filter({$0.isKeyWindow}).first?.rootViewController else { return }
    controller.dismiss(animated: true)
  }

  @objc func destroyDB() {
    guard let core = self.core else { return }
    core.destroyStorage { _ in }
  }

  @objc func retrieveConversations(_ completion: @escaping RCTResponseSenderBlock) {
    guard let core = self.core else {
      completion([])
      return
    }

    let topQueryGroup = DispatchGroup()
    var results: [[String:String]] = []

    // Execute a query on the core configuration to retrieve active conversations the local user is a part of.
    // This is a paginated API, you can retrieve additional pages by passing in a reference to the oldest conversation in the previous
    // page to `olderThanConversation`
    core.conversations(withLimit: 0, olderThanConversation: nil, completion: { conversations, error in
      if (error != nil) {
        completion([])
        return
      }

      // Construct a JSON object composed of an array of dictionaries in the form of:
      // [{
      //    "conversationId": "<resolved-conversationId>"
      //    "lastMessageText": "<resolved-last-message-text>"
      //  }]
      //
      // As configured this will just return the last entry on the last conversation.
      conversations?.forEach({ conversation in
        topQueryGroup.enter()

        let client = core.conversationClient(with: conversation.identifier)
        var payload: [String:String] = [:]
        var lastMessageText: String = ""

        payload["conversationId"] = conversation.identifier.uuidString

        client.entries(withLimit: 0, fromTimestamp: nil, direction: .descending) { entries, _, entryError in
          if (error != nil) {
            topQueryGroup.leave()
            return
          }

          // As an example we'll just extract the latest entry which is a textMessage
          if let entry = entries?.first(where: { $0.format == .textMessage }) {
            lastMessageText = self.previewText(entry)
          }

          payload["lastMessageText"] = lastMessageText
          results.append(payload)
          topQueryGroup.leave()
        }
      })

      topQueryGroup.notify(queue: .main) {
        guard let data = try? JSONSerialization.data(withJSONObject: results, options: [.prettyPrinted, .sortedKeys]) else {
          completion([])
          return
        }

        guard let result = String(data: data, encoding: .utf8) else {
          completion([])
          return
        }

        completion([result])
        return
      }
    })
  }
}

@available(iOS 14.1, *)
extension InterfaceViewController {
  // This is required for consumers using RNN https://reactnative.dev/docs/navigation
  // RNSNavigationController makes an unsafe call to a 'screenView' property without checking for implementation
  // which will result in a crash.
  // Adding this extension allows you to push native views with animation
  @objc func screenView() -> UIView? { return nil }
}

@available(iOS 14.1, *)
extension InAppModule: CoreDelegate {
  @objc func core(_ core: CoreClient, didError error: Error) {
    print("Error: \(String(describing: error))")
  }

  @objc func core(_ core: CoreClient, conversation: Conversation, didReceiveEntries entries: [ConversationEntry], paged: Bool) {
    for entry: ConversationEntry? in entries {
      if let error = entry?.error {
        print("Error: \(String(describing: error))")
      }
    }
  }
}
