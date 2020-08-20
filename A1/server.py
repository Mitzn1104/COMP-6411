import socketserver

d = {}


def load_data():
    print("[LOADING] Server is loading data...")
    with open("data.txt") as f:
        for line in f:
            my_list = line.split("|")
            if my_list[0]:
                person = (my_list[0].strip(), my_list[1].strip(), my_list[2].strip(), my_list[3].strip())
                d[my_list[0].strip()] = person
    print("[LOADING COMPLETE] Server data load completed.")


def recvall(sock):
    BSIZE = 4096
    rec_data = b''
    while True:
        part = sock.recv(BSIZE)
        rec_data += part
        if len(part) < BSIZE:
            break
    return rec_data


def send_all_data(sock):
    all_records = 'Name,Age,Address,Phone#\n'
    for key in sorted(d.keys()):
        all_records += d[key][0] + "," + d[key][1] + "," + d[key][2] + "," + d[key][3] + "," + "\n"
    sock.sendall(bytes(all_records, "utf-8"))


def find_customer(sock, customer_name):
    key = d.get(customer_name)
    if key:
        sock.sendall(bytes('Name,Age,Address,Phone#\n' + key[0] + "," + key[1] + "," + key[2] + "," + key[3] +
                           "\n", "utf-8"))
    else:
        sock.sendall(bytes("Customer not found", "utf-8"))


def add_customer(sock, customer_details):
    customer_details_list = customer_details.split(",")
    if customer_details_list[0] in d.keys():
        response = "Customer already exits"
    else:
        p = (customer_details_list[0].strip(), customer_details_list[1], customer_details_list[2].strip(),
             customer_details_list[3].strip())
        d[customer_details_list[0].strip()] = p
        response = "Customer has been added"
    sock.sendall(bytes(str(response), "utf-8"))


def delete_customer(sock, customer_name):
    customer_response = d.get(customer_name, "Customer does not exist")
    if str(customer_response) != "Customer does not exist":
        del d[customer_name]
        customer_response = "Customer deleted"
    sock.sendall(bytes(str(customer_response), "utf-8"))


def update_customer(sock, customer_details, option):
    customer_details_list = customer_details.split(",")
    if customer_details_list[0] in d.keys():
        customer = d.get(customer_details_list[0])
        cust_list = list(customer)
        cust_list[int(option)] = customer_details_list[1]
        customer = tuple(cust_list)
        d[customer_details_list[0]] = customer
        response = "Customer has been updated"
    else:
        response = "Customer not found"
    sock.sendall(bytes(response, "utf-8"))


class MyTCPHandler(socketserver.BaseRequestHandler):

    def handle(self):

        while True:
            choice = recvall(self.request).strip()
            choice = choice.decode('ASCII')
            choice_list = choice.split("|")
            print('Client selected :' + choice_list[0])
            if choice_list[0] == '1':
                # Find Customer
                find_customer(self.request, choice_list[1])
            elif choice_list[0] == '2':
                # Add Customer
                add_customer(self.request, choice_list[1])
            elif choice_list[0] == '3':
                # Delete Customer
                delete_customer(self.request, choice_list[1])
            elif choice_list[0] == '4':
                # Update Customer Age
                update_customer(self.request, choice_list[1], '1')
            elif choice_list[0] == '5':
                # Update Customer address
                update_customer(self.request, choice_list[1], '2')
            elif choice_list[0] == '6':
                # Update Customer phone
                update_customer(self.request, choice_list[1], '3')
            elif choice_list[0] == '7':
                # Print Report
                send_all_data(self.request)
            elif choice_list[0] == '8':
                # Exit
                self.request.sendall(bytes("Good bye", "utf-8"))
                break


if __name__ == "__main__":
    HOST, PORT = "localhost", 9999

    # Create the server, binding to localhost on port 9999
    with socketserver.TCPServer((HOST, PORT), MyTCPHandler) as server:
        # Activate the server; this will keep running until you
        # interrupt the program with Ctrl-C
        print("[STARTING] Server is starting...")
        print(f"[LISTENING] Server is listening on {server.server_address}")
        load_data()
        server.serve_forever()
